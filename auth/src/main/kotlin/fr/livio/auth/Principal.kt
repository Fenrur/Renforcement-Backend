package fr.livio.auth

import io.quarkus.oidc.runtime.OidcJwtCallerPrincipal
import jakarta.json.JsonObject
import jakarta.json.JsonString
import java.util.*

data class Principal(
    val id: UUID,
    val email: String,
    val emailVerified: Boolean,
    val role: PrincipalRole
)

fun java.security.Principal.decode(): Principal {
    if (this !is OidcJwtCallerPrincipal) throw IllegalArgumentException("Principal is not an OidcJwtCallerPrincipal")

    val realmAccess = claims.getClaimValue("realm_access") as JsonObject
    val roles = realmAccess.getJsonArray("roles")

    if (roles.isEmpty()) throw IllegalArgumentException("No roles found in realm_access")

    val roleName = (roles[0] as JsonString).string

    return Principal(
        id = UUID.fromString(claims.subject),
        email = claims.getClaimValue("email").toString(),
        emailVerified = claims.getClaimValue("email_verified").toString().toBoolean(),
        role = PrincipalRole.from(roleName)
    )
}