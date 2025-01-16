package fr.livio

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import io.smallrye.mutiny.Uni
import io.vertx.mutiny.ext.consul.ConsulClient
import io.vertx.mutiny.ext.web.client.WebClient
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class ConfigService(private val webClient: WebClient, private val consulClient: ConsulClient) {

    private val yamlMapper = ObjectMapper(YAMLFactory())

    private fun randomServiceUrl(): Uni<String> {
        return consulClient
            .healthServiceNodes("config", true)
            .onItem().transform { serviceEntryList ->
                val serviceEntry = serviceEntryList.list.randomOrNull()
                    ?: throw IllegalStateException("No service found for config")

                val address = serviceEntry.node.address
                val port = serviceEntry.service.port
                "http://$address:$port"
            }
    }

    fun getRawConfig(name: String): Uni<String> = randomServiceUrl()
        .onItem().transformToUni { serviceUrl ->
            val finalUrl = "$serviceUrl$name"
            val request = webClient.getAbs(finalUrl)
            request.send()
        }
        .onItem().transform { response ->
            response.bodyAsString()
        }
    
    fun <T> getAndParse(name: String, clazz: Class<T>): Uni<T> = getRawConfig(name)
        .onItem().transform { contentConfig ->
            yamlMapper.readValue(contentConfig, clazz)
        }
}