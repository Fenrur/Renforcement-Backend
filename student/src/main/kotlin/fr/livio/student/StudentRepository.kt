package fr.livio.student

import io.quarkus.mongodb.panache.PanacheMongoRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class StudentRepository: PanacheMongoRepository<MongoStudent>