package fr.livio.gateway

import io.smallrye.mutiny.Uni
import io.vertx.core.http.HttpMethod
import io.vertx.mutiny.ext.consul.ConsulClient
import io.vertx.mutiny.ext.web.Router
import io.vertx.mutiny.ext.web.client.WebClient
import io.vertx.mutiny.ext.web.handler.BodyHandler
import jakarta.enterprise.context.ApplicationScoped

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

        router.route(method, pathIn).handler { ctx ->

            randomServiceUrl(serviceOut)
                .onItem().transformToUni { serviceUrl ->
                    var finalOutUrl = serviceUrl + pathOut
                    
                    ctx.pathParams().forEach {
                        finalOutUrl = finalOutUrl.replace(":${it.key}", it.value)
                    }

                    val clientRequest = webClient.requestAbs(method, finalOutUrl)

                    for (headerName in ctx.request().headers().names()) {
                        if (
                            !headerName.equals("Host", ignoreCase = true) &&
                            !headerName.equals("Content-Length", ignoreCase = true)
                        ) {
                            val headerValue = ctx.request().getHeader(headerName)
                            clientRequest.putHeader(headerName, headerValue)
                        }
                    }

                    val queryParams = ctx.queryParams()
                    queryParams.names().forEach { paramName ->
                        queryParams.getAll(paramName).forEach { paramValue ->
                            clientRequest.addQueryParam(paramName, paramValue)
                        }
                    }

                    val bodyBuffer = ctx.body().buffer()
                    
                    if (bodyBuffer == null) clientRequest.send() else clientRequest.sendBuffer(bodyBuffer)
                }
                .onItem().transformToUni { response ->
                    ctx.response().setStatusCode(response.statusCode())
                    response.headers().forEach { entry ->
                        ctx.response().headers().set(entry.key, entry.value)
                    }

                    val bodyBuffer = response.bodyAsBuffer()
                    
                    if (bodyBuffer == null) ctx.response().end() else ctx.response().end(bodyBuffer)
                }
                .subscribe().with(
                    { },
                    { error -> ctx.fail(error) }
                )
        }
        
        router.delegate.route().last().handler { ctx ->
            ctx.response().setStatusCode(404).end()
        }
    }
}