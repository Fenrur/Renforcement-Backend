package fr.livio

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient

@Path("/school")
@RegisterRestClient
interface SchoolService {
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun get(@QueryParam("id") id: Int): School
}