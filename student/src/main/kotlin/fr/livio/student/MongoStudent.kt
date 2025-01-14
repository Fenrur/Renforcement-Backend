package fr.livio.student

import fr.livio.Gender
import fr.livio.Student
import io.quarkus.mongodb.panache.PanacheMongoEntity
import io.quarkus.mongodb.panache.common.MongoEntity
import org.bson.types.ObjectId

@MongoEntity
data class MongoStudent(var id: ObjectId?, var name: String?, var gender: Gender?, var schoolId: Int?) {
    constructor() : this(null, null, null,null)
    
    fun toStudent() = Student(id!!.toString(), name!!, gender!!)
}