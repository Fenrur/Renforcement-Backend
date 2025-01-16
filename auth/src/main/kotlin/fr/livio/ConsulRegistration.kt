package fr.livio

import io.quarkus.logging.Log
import io.quarkus.runtime.ShutdownEvent
import io.quarkus.runtime.StartupEvent
import io.quarkus.runtime.configuration.ConfigUtils
import io.vertx.ext.consul.ServiceOptions
import io.vertx.mutiny.ext.consul.ConsulClient
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.security.SecureRandom
import java.util.OptionalInt
import java.util.Optional

@ApplicationScoped
class ConsulRegistration(
    private val consultClient: ConsulClient,
    @ConfigProperty(name = "consul.name") private val serviceName: String
) {

    private val id: Int = SecureRandom.getInstanceStrong().nextInt(9999)
    
    fun onStart(
        @Observes ev: StartupEvent,
        @ConfigProperty(name = "quarkus.http.port") port: Int,
        @ConfigProperty(name = "quarkus.http.host") host: String,
        @ConfigProperty(name = "quarkus.keycloak.devservices.port") keycloakPort: OptionalInt,
        @ConfigProperty(name = "quarkus.keycloak.devservices.host") keycloakHost: Optional<String>,
    ) {
        val option = ServiceOptions()
            .setName(serviceName)
            .setPort(port)
            .setAddress(host)
            .setId("$serviceName-$id")

        consultClient.registerServiceAndAwait(option)

        Log.info("Consul service registered with id $serviceName-$id")
        
        if (ConfigUtils.isProfileActive("dev")) {
            val keycloakPort = keycloakPort.orElseThrow()
            val keycloakHost = keycloakHost.orElse("localhost")
            
            val keycloakOption = ServiceOptions()
                .setName("keycloak")
                .setPort(keycloakPort)
                .setAddress(keycloakHost)
                .setId("keycloak-$id")
            
            consultClient.registerServiceAndAwait(keycloakOption)
        }
    }
    
    fun onStop(@Observes ev: ShutdownEvent, ) {
        consultClient.deregisterServiceAndAwait("$serviceName-$id")
        consultClient.deregisterServiceAndAwait("keycloak-$id")
        consultClient.close()
    }
}