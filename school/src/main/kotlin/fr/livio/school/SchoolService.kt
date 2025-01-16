package fr.livio.school

import fr.livio.School
import fr.livio.jooq.tables.daos.SchoolsDao
import fr.livio.jooq.tables.pojos.Schools
import fr.livio.jooq.tables.references.SCHOOLS
import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import org.jooq.Configuration
import org.jooq.DSLContext

@ApplicationScoped
class SchoolService(private val schoolsDao: SchoolsDao, private val dslContext: DSLContext) {

    fun create(name: String, address: String, directorName: String): Int = dslContext.transactionResult { trx: Configuration ->
        val id = trx.dsl().insertInto(SCHOOLS, SCHOOLS.NAME, SCHOOLS.ADDRESS, SCHOOLS.DIRECTORNAME)
            .values(name, address, directorName)
            .returningResult(SCHOOLS.ID)
            .fetchOne()
            ?.value1()

        return@transactionResult id ?: throw IllegalStateException("School not created")
    }

    fun get(id: Int): School? {
        val schools = schoolsDao.findById(id)

        return if (schools != null) School(schools.id!!, schools.name, schools.address, schools.directorname) else null
    }

    fun update(id: Int, name: String, address: String, directorName: String) =
        schoolsDao.update(Schools(id, name, address, directorName))
    
    fun delete(id: Int) = schoolsDao.deleteById(id)

}