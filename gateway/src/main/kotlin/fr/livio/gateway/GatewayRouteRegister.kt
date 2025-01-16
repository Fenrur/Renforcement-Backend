package fr.livio.gateway

import fr.livio.ConfigService
import io.quarkus.runtime.StartupEvent
import io.vertx.core.http.HttpMethod
import io.vertx.mutiny.ext.web.Router
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes

@ApplicationScoped
class GatewayRouteRegister(
    private val gtwRouter: GatewayRouter,
    private val router: Router,
    private val configService: ConfigService
    ) {

    fun onStart(@Observes ev: StartupEvent) {
        val configuration = configService.getAndParse("/gateway.yaml", GatewayRouteConfiguration::class.java).await().indefinitely()

        println(configuration)
        
        configuration.gatewayRoutes.forEach { routingElement ->
            if (routingElement.method != null) {
                gtwRouter.registerRoute(
                    method = HttpMethod(routingElement.method),
                    pathIn = routingElement.pathIn,
                    pathOut = routingElement.pathIn,
                    serviceOut =routingElement.serviceOut
                )
            } else {
                gtwRouter.registerRoute(
                    pathIn = routingElement.pathIn,
                    pathOut = routingElement.pathIn,
                    serviceOut =routingElement.serviceOut
                )
            }
        }

        router.delegate.route().last().handler { ctx ->
            ctx.response().setStatusCode(404).end()
        }
    }
}