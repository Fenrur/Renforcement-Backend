package fr.livio

import io.vertx.core.Vertx
import io.vertx.mutiny.ext.web.client.WebClient
import jakarta.enterprise.inject.Produces

class WebClientProducer {
    
    @Produces
    fun webClient(vertx: Vertx): WebClient = WebClient(io.vertx.ext.web.client.WebClient.create(vertx))
}