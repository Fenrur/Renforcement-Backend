package fr.livio.gateway

import io.quarkus.runtime.StartupEvent
import io.vertx.core.http.HttpMethod
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes

@ApplicationScoped
class GatewayRouteRegister(private val router: GatewayRouter) {
    
    fun onStart(@Observes ev: StartupEvent) {
        registerSchoolRoutes()
        registerStudentRoutes()
    }

    private fun registerSchoolRoutes() {
        router.registerRoute(
            method = HttpMethod.POST,
            serviceOut = "school",
            pathIn = "/school",
            pathOut = "/school",
        )

        router.registerRoute(
            method = HttpMethod.GET,
            serviceOut = "school",
            pathIn = "/school/:id",
            pathOut = "/school/:id",
        )

        router.registerRoute(
            method = HttpMethod.PUT,
            serviceOut = "school",
            pathIn = "/school/:id",
            pathOut = "/school/:id",
        )

        router.registerRoute(
            method = HttpMethod.DELETE,
            serviceOut = "school",
            pathIn = "/school/:id",
            pathOut = "/school/:id",
        )
    }
    
    private fun registerStudentRoutes() {
        router.registerRoute(
            method = HttpMethod.POST,
            serviceOut = "student",
            pathIn = "/student",
            pathOut = "/student",
        )

        router.registerRoute(
            method = HttpMethod.GET,
            serviceOut = "student",
            pathIn = "/student",
            pathOut = "/student",
        )

        router.registerRoute(
            method = HttpMethod.PUT,
            serviceOut = "student",
            pathIn = "/student/:id",
            pathOut = "/student/:id",
        )

        router.registerRoute(
            method = HttpMethod.DELETE,
            serviceOut = "student",
            pathIn = "/student/:id",
            pathOut = "/student/:id",
        )
    }
}