package fr.livio.auth

import java.util.concurrent.StructuredTaskScope

inline fun <T> shutdownOnFailureTaskScope(block: (StructuredTaskScope.ShutdownOnFailure) -> T): T {
    val result: T
    StructuredTaskScope.ShutdownOnFailure().use { scope ->
        result = block(scope)
    }

    return result
}