package fr.livio

data class School(val id: Int, val name: String, val address: String, val directorName: String)
data class Student(val id: String, val name: String, val gender: Gender)

enum class Gender {
    Male,
    Female
}