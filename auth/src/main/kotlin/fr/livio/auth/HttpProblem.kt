package fr.livio.auth

import io.quarkiverse.resteasy.problem.HttpProblem
import jakarta.ws.rs.core.Response
import java.net.URI

fun problem(status: Response.StatusType, title: String? = null, detail: String? = null, instance: URI? = null, problemType: URI? = null): HttpProblem =
    HttpProblem.builder()
        .withStatus(status)
        .withTitle(title)
        .withDetail(detail)
        .withInstance(instance)
        .withType(problemType)
        .build()