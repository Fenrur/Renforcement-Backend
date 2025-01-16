package fr.livio.gateway

import io.quarkus.runtime.StartupEvent
import io.vertx.core.http.HttpMethod
import io.vertx.mutiny.ext.web.Router
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes

@ApplicationScoped
class GatewayRouteRegister(private val gtwRouter: GatewayRouter, private val router: Router) {

    fun onStart(@Observes ev: StartupEvent) {
        registerSchoolRoutes()
        registerStudentRoutes()
        registerAuthRoutes()

        router.delegate.route().last().handler { ctx ->
            ctx.response().setStatusCode(404).end()
        }
    }

    private fun registerAuthRoutes() {
        gtwRouter.registerAllRoutes("keycloak", "/auth", "/")
    }

    private fun registerSchoolRoutes() {
        gtwRouter.registerRoute(
            method = HttpMethod.POST,
            serviceOut = "school",
            pathIn = "/school",
            pathOut = "/school",
        )

        gtwRouter.registerRoute(
            method = HttpMethod.GET,
            serviceOut = "school",
            pathIn = "/school",
            pathOut = "/school",
        )

        gtwRouter.registerRoute(
            method = HttpMethod.PUT,
            serviceOut = "school",
            pathIn = "/school/:id",
            pathOut = "/school/:id",
        )

        gtwRouter.registerRoute(
            method = HttpMethod.DELETE,
            serviceOut = "school",
            pathIn = "/school/:id",
            pathOut = "/school/:id",
        )
    }

    private fun registerStudentRoutes() {
        gtwRouter.registerRoute(
            method = HttpMethod.POST,
            serviceOut = "student",
            pathIn = "/student",
            pathOut = "/student",
        )

        gtwRouter.registerRoute(
            method = HttpMethod.GET,
            serviceOut = "student",
            pathIn = "/student",
            pathOut = "/student",
        )

        gtwRouter.registerRoute(
            method = HttpMethod.PUT,
            serviceOut = "student",
            pathIn = "/student/:id",
            pathOut = "/student/:id",
        )

        gtwRouter.registerRoute(
            method = HttpMethod.DELETE,
            serviceOut = "student",
            pathIn = "/student/:id",
            pathOut = "/student/:id",
        )
    }
}