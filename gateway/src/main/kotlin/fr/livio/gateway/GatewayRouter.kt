package fr.livio.gateway

import io.smallrye.mutiny.Uni
import io.vertx.core.http.HttpMethod
import io.vertx.mutiny.core.buffer.Buffer
import io.vertx.mutiny.ext.consul.ConsulClient
import io.vertx.mutiny.ext.web.Router
import io.vertx.mutiny.ext.web.RoutingContext
import io.vertx.mutiny.ext.web.client.HttpResponse
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
    
    
    fun registerAllRoutes(
        serviceOut: String,
        pathIn: String,
        pathOut: String
    ) {
        
        router.route("$pathIn/*").handler(BodyHandler.create())
        
        router.route("$pathIn/*").handler { ctx ->
            randomServiceUrl(serviceOut)
                .onItem().transformToUni { serviceUrl ->
                    val finalOutUrl = serviceUrl + ctx.request().path().replace(pathIn, pathOut)

                    sendToService(ctx, finalOutUrl)
                }
                .onItem().transformToUni { response ->
                    responseToClient(ctx, response)
                }
                .subscribe().with(
                    { },
                    { error -> ctx.fail(error) }
                )
        }

        router.route(pathIn).handler(BodyHandler.create())

        router.route(pathIn).handler { ctx ->
            randomServiceUrl(serviceOut)
                .onItem().transformToUni { serviceUrl ->
                    val finalOutUrl = serviceUrl + ctx.request().path().replace(pathIn, pathOut)

                    sendToService(ctx, finalOutUrl)
                }
                .onItem().transformToUni { response ->
                    responseToClient(ctx, response)
                }
                .subscribe().with(
                    { },
                    { error -> ctx.fail(error) }
                )
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

                    sendToService(ctx, finalOutUrl)
                }
                .onItem().transformToUni { response ->
                    responseToClient(ctx, response)
                }
                .subscribe().with(
                    { },
                    { error -> ctx.fail(error) }
                )
        }
    }

    private fun responseToClient(
        ctx: RoutingContext,
        response: HttpResponse<Buffer>,
    ): Uni<Void>? {
        ctx.response().statusCode = response.statusCode()

        response.headers().forEach { entry ->
            ctx.response().headers().set(entry.key, entry.value)
        }

        val bodyBuffer = response.bodyAsBuffer()

        return if (bodyBuffer == null) ctx.response().end() else ctx.response().end(bodyBuffer)
    }

    private fun sendToService(
        ctx: RoutingContext,
        finalOutUrl: String,
    ): Uni<HttpResponse<Buffer>>? {
        val clientRequest = webClient.requestAbs(ctx.request().method(), finalOutUrl)

        val headersRequest = ctx.request().headers()

        headersRequest
            .names()
            .filter { headerName -> !headerName.equals("Host", ignoreCase = true) }
            .map { headerName -> clientRequest.putHeader(headerName, headersRequest.get(headerName)) }


        val queryParamsRequest = ctx.queryParams()

        queryParamsRequest.names().forEach { paramName ->
            queryParamsRequest.getAll(paramName).forEach { paramValue ->
                clientRequest.addQueryParam(paramName, paramValue)
            }
        }

        val bodyBuffer = ctx.body().buffer()

        return if (bodyBuffer == null) clientRequest.send() else clientRequest.sendBuffer(bodyBuffer)
    }
}