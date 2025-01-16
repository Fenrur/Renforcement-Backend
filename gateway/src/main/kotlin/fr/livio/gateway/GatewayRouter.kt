package fr.livio.gateway

import io.quarkus.logging.Log
import io.smallrye.mutiny.Uni
import io.vertx.core.http.HttpMethod
import io.vertx.mutiny.core.buffer.Buffer
import io.vertx.mutiny.ext.consul.ConsulClient
import io.vertx.mutiny.ext.web.Router
import io.vertx.mutiny.ext.web.RoutingContext
import io.vertx.mutiny.ext.web.client.WebClient
import io.vertx.mutiny.ext.web.client.HttpResponse
import io.vertx.mutiny.ext.web.handler.BodyHandler
import jakarta.enterprise.context.ApplicationScoped
import java.util.function.Consumer

@ApplicationScoped
class GatewayRouter(
    private val router: Router,
    private val consulClient: ConsulClient,
    private val webClient: WebClient,
) {

    private fun randomServiceUrl(serviceName: String): Uni<String> {
        return consulClient
            .healthServiceNodes(serviceName, true)
            .onItem().transform { serviceEntryList ->
                val serviceEntry = serviceEntryList.list.randomOrNull()
                    ?: throw IllegalStateException("No service found for $serviceName")

                val address = serviceEntry.node.address
                val port = serviceEntry.service.port
                "http://$address:$port"
            }
    }

    fun registerRoute(
        method: HttpMethod,
        serviceOut: String,
        pathIn: String,
        pathOut: String,
    ) {
        router.route(method, pathIn).handler(BodyHandler.create())
        router.route(method, pathIn).handler(GatewayRouteHandler(serviceOut, pathIn, pathOut))
    }

    fun registerRoute(
        serviceOut: String,
        pathIn: String,
        pathOut: String
    ) {
        router.route(pathIn).handler(BodyHandler.create())
        router.route(pathIn).handler(GatewayRouteHandler(serviceOut, pathIn, pathOut))
    }

    inner class GatewayRouteHandler(
        private val serviceOut: String,
        private val pathIn: String,
        private val pathOut: String,
    ) : (RoutingContext) -> Unit {
        override fun invoke(ctx: RoutingContext) {
            randomServiceUrl(serviceOut)
                .onItem().transformToUni { serviceUrl ->
                    var finalOutUrl = serviceUrl + ctx.request().path().replace(pathIn, pathOut)

                    ctx.pathParams().forEach {
                        finalOutUrl = finalOutUrl.replace(":${it.key}", it.value)
                    }

                    Log.info("Forwarding to: $finalOutUrl")

                    val clientRequest = webClient.requestAbs(ctx.request().method(), finalOutUrl)

                    val headersRequest = ctx.request().headers()
                    headersRequest
                        .names()
                        .filter { headerName -> !headerName.equals("Host", ignoreCase = true) }
                        .forEach { headerName ->
                            clientRequest.putHeader(headerName, headersRequest.get(headerName))
                        }

                    val originalXForwardedFor = headersRequest.get("X-Forwarded-For")
                    val remoteAddress = ctx.request().remoteAddress().host() // IP du client
                    val newXForwardedFor = if (originalXForwardedFor != null) {
                        "$originalXForwardedFor, $remoteAddress"
                    } else {
                        remoteAddress
                    }
                    clientRequest.putHeader("X-Forwarded-For", newXForwardedFor)

                    val hostHeader = headersRequest.get("Host") ?: ctx.request().host()
                    clientRequest.putHeader("X-Forwarded-Host", hostHeader)

                    val originalXForwardedProto = headersRequest.get("X-Forwarded-Proto")
                    val newXForwardedProto = originalXForwardedProto ?: if (ctx.request().isSSL) "https" else "http"
                    clientRequest.putHeader("X-Forwarded-Proto", newXForwardedProto)

                    val originalXForwardedPort = headersRequest.get("X-Forwarded-Port")
                    if (originalXForwardedPort == null) {
                        val port = if (ctx.request().isSSL) "443" else "80"
                        clientRequest.putHeader("X-Forwarded-Port", port)
                    }

                    val queryParamsRequest = ctx.queryParams()
                    queryParamsRequest.names().forEach { paramName ->
                        queryParamsRequest.getAll(paramName).forEach { paramValue ->
                            clientRequest.addQueryParam(paramName, paramValue)
                        }
                    }

                    val bodyBuffer = ctx.body().buffer()
                    if (bodyBuffer == null) clientRequest.send()
                    else clientRequest.sendBuffer(bodyBuffer)
                }
                .onItem().transformToUni { response ->
                    ctx.response().statusCode = response.statusCode()

                    response.headers().forEach { entry ->
                        ctx.response().headers().set(entry.key, entry.value)
                    }

                    // Copie le corps de la réponse
                    val bodyBuffer = response.bodyAsBuffer()
                    if (bodyBuffer == null) ctx.response().end()
                    else ctx.response().end(bodyBuffer)
                }
                .subscribe().with(
                    { },
                    { error -> ctx.fail(error) }
                )
        }
    }
}