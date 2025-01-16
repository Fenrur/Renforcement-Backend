package fr.livio.gateway

import fr.livio.ConfigService
import io.quarkus.logging.Log
import io.quarkus.scheduler.Scheduled
import io.vertx.core.http.HttpMethod
import io.vertx.mutiny.ext.web.Router
import jakarta.enterprise.context.ApplicationScoped
import java.util.LinkedHashSet

@ApplicationScoped
class GatewayRouteRegister(
    private val gtwRouter: GatewayRouter,
    private val router: Router,
    private val configService: ConfigService
    ) {

    var currentConfiguration: GatewayRouteConfiguration = GatewayRouteConfiguration(LinkedHashSet())
    
    @Scheduled(every = "5s", identity = "gateway-config")
    fun scheduled() {
        try {
            val configuration = configService.getAndParse("/gateway.yaml", GatewayRouteConfiguration::class.java).await().indefinitely()

            if (configuration == this.currentConfiguration) {
                return
            }
            
            this.currentConfiguration = configuration
            
            gtwRouter.clear()
            
            configuration.gatewayRoutes.forEach { routingElement ->
                if (routingElement.method == "*") {
                    gtwRouter.registerRoute(
                        pathIn = routingElement.pathIn,
                        pathOut = routingElement.pathOut,
                        serviceOut = routingElement.serviceOut
                    )
                } else {
                    gtwRouter.registerRoute(
                        method = HttpMethod(routingElement.method),
                        pathIn = routingElement.pathIn,
                        pathOut = routingElement.pathOut,
                        serviceOut = routingElement.serviceOut
                    )
                }
            }

            router.delegate.route().last().handler { ctx ->
                ctx.response().setStatusCode(404).end()
            }
        } catch (e: Exception) {
            Log.warn("Error while parsing gateway configuration", e)
        }
    }
}