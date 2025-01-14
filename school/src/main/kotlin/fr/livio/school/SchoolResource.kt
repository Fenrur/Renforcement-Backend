package fr.livio.school

import fr.livio.School
import io.quarkus.logging.Log
import io.smallrye.common.annotation.RunOnVirtualThread
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import org.jboss.resteasy.reactive.ResponseStatus
import org.jboss.resteasy.reactive.RestResponse

@Path("/school")
@RunOnVirtualThread
class SchoolResource(private val schoolService: SchoolService) {

    data class CreateSchoolRequestBody(val name: String, val address: String, val directorName: String)
    data class CreateSchoolResponseBody(val id: Int, val name: String, val address: String, val directorName: String)
    
    @POST
    @ResponseStatus(RestResponse.StatusCode.CREATED)
    fun create(body: CreateSchoolRequestBody): CreateSchoolResponseBody {
        val id = schoolService.create(body.name, body.address, body.directorName)
        
        return CreateSchoolResponseBody(id, body.name, body.address, body.directorName)
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ResponseStatus(RestResponse.StatusCode.OK)
    fun get(@QueryParam("id") id: Int): School = schoolService.get(id) ?: throw NotFoundException("School not found")
    
    data class UpdateSchoolRequestBody(val name: String, val address: String, val directorName: String)
    
    @PUT
    @Path("/{id}")
    fun update(@PathParam("id") id: Int, body: UpdateSchoolRequestBody): Unit = 
        schoolService.update(id, body.name, body.address, body.directorName)
    
    @DELETE
    @Path("/{id}")
    fun delete(@PathParam("id") id: Int): Unit = schoolService.delete(id)
}