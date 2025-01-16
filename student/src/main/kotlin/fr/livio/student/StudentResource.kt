package fr.livio.student

import fr.livio.Gender
import fr.livio.School
import fr.livio.Student
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import org.jboss.resteasy.reactive.ResponseStatus
import org.jboss.resteasy.reactive.RestResponse

@Path("/student")
class StudentResource(private val studentService: StudentService) {
    
    data class CreateStudentRequestBody(val name: String, val gender: Gender, val schoolId: Int)
    data class CreateStudentResponseBody(val student: Student, val school: School)
    
    @POST
    @ResponseStatus(RestResponse.StatusCode.CREATED)
    fun create(body: CreateStudentRequestBody): CreateStudentResponseBody = studentService
        .create(body.name, body.gender, body.schoolId)
        .let { CreateStudentResponseBody(it.student, it.school) }
    
    data class GetStudentResponseBody(val student: Student, val school: School)
    
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun get(@PathParam("id") id: String): GetStudentResponseBody = studentService
        .get(id)
        .let { GetStudentResponseBody(it.student, it.school) }
    
    data class UpdateStudentRequestBody(val name: String, val gender: Gender, val schoolId: Int)
    data class UpdateStudentResponseBody(val student: Student, val school: School)
    
    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun update(id: String, body: UpdateStudentRequestBody): UpdateStudentResponseBody = studentService
        .update(id, body.name, body.gender, body.schoolId)
        .let { UpdateStudentResponseBody(it.student, it.school) }
    
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun delete(id: String): Unit = studentService
        .delete(id)
        .let { Unit }
}