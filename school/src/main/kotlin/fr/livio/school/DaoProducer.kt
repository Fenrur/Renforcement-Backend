package fr.livio.school

import fr.livio.jooq.tables.daos.SchoolsDao
import jakarta.enterprise.inject.Produces
import org.jooq.Configuration

class DaoProducer {

    @Produces
    fun schoolsDao(conf: Configuration): SchoolsDao = SchoolsDao(conf)
}