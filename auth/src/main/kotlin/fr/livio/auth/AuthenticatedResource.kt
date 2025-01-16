package fr.livio.auth

import io.quarkus.security.Authenticated
import io.quarkus.security.identity.SecurityIdentity
import io.smallrye.common.annotation.RunOnVirtualThread
import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.openapi.annotations.tags.Tag

@Path("/iam")
@Authenticated
@Tag(name = "AuthenticatedResource", description = "Resource to test roles")
@RunOnVirtualThread
class AuthenticatedResource(
    private val identity: SecurityIdentity,
) {

    @POST
    @Path("/user")
    @Produces(MediaType.TEXT_PLAIN)
    @RolesAllowed("user")
    fun user(): String = "You are a user"

    @POST
    @Path("/admin")
    @Produces(MediaType.TEXT_PLAIN)
    @RolesAllowed("admin")
    fun admin(): String = "You are an admin"

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/principal")
    fun getPrincipal(): Principal = identity.principal.decode()
}