package fr.livio.student

import fr.livio.Gender
import fr.livio.School
import fr.livio.SchoolService
import fr.livio.Student
import jakarta.enterprise.context.ApplicationScoped
import org.bson.types.ObjectId
import org.eclipse.microprofile.rest.client.inject.RestClient

@ApplicationScoped
class StudentService(
    @RestClient private val schoolService: SchoolService,
    private val studentRepository: StudentRepository,
) {

    data class CreateStudentResult(val student: Student, val school: School)

    fun create(name: String, gender: Gender, schoolId: Int): CreateStudentResult {
        val school = schoolService.get(schoolId)

        val mongoStudent = MongoStudent(null, name, gender, schoolId)
        studentRepository.persist(mongoStudent)
        
        val student = Student(mongoStudent.id.toString(), name, gender)

        return CreateStudentResult(student, school)
    }
    
    data class GetStudentResult(val student: Student, val school: School)
    
    fun get(id: String): GetStudentResult {
        val mongoStudent = studentRepository.findById(ObjectId(id)) ?: throw IllegalStateException("Student not found")
        val student = mongoStudent.toStudent()

        val schoolId = mongoStudent.schoolId ?: throw IllegalStateException("Student not associated with a school")
        val school = schoolService.get(schoolId)

        return GetStudentResult(student, school)
    }
    
    data class UpdateStudentResult(val student: Student, val school: School)

    fun update(id: String, name: String, gender: Gender, schoolId: Int): UpdateStudentResult {
        val school = schoolService.get(schoolId)

        val mongoStudent = studentRepository.findById(ObjectId(id)) ?: throw IllegalStateException("Student not found")
        mongoStudent.name = name
        mongoStudent.gender = gender
        mongoStudent.schoolId = schoolId
        
        studentRepository.update(mongoStudent)

        val student = Student(mongoStudent.id.toString(), name, gender)
        
        return UpdateStudentResult(student, school)
    }
    
    data class DeleteStudentResult(val student: Student, val school: School)
    
    fun delete(id: String): DeleteStudentResult {
        val mongoStudent = studentRepository.findById(ObjectId(id)) ?: throw IllegalStateException("Student not found")
        val student = mongoStudent.toStudent()

        val schoolId = mongoStudent.schoolId ?: throw IllegalStateException("Student not associated with a school")
        val school = schoolService.get(schoolId)

        studentRepository.deleteById(ObjectId(id))
        
        return DeleteStudentResult(student, school)
    }
}