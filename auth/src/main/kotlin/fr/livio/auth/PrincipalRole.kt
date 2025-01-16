package fr.livio.auth

enum class PrincipalRole {
    USER,
    PARTNER,
    ADMIN;

    companion object {
        fun from(roleName: String): PrincipalRole {
            val r = roleName.lowercase()

            return when (r) {
                "user" -> USER
                "partner" -> PARTNER
                "admin" -> ADMIN
                else -> throw IllegalArgumentException("Unknown role: $roleName")
            }
        }
    }
}