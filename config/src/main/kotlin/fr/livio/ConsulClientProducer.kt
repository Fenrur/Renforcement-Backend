package fr.livio

import io.vertx.ext.consul.ConsulClientOptions
import io.vertx.mutiny.core.Vertx
import io.vertx.mutiny.ext.consul.ConsulClient
import jakarta.enterprise.inject.Produces
import org.eclipse.microprofile.config.inject.ConfigProperty

class ConsulClientProducer {

    @Produces
    fun consulClient(
        vertx: Vertx,
        @ConfigProperty(name = "consul.host") host: String,
        @ConfigProperty(name = "consul.port") port: Int
    ): ConsulClient = ConsulClient.create(vertx, ConsulClientOptions().setHost(host).setPort(port))
    
}