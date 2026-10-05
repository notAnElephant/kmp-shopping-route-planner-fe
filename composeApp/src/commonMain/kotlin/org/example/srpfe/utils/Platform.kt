package org.example.srpfe.utils

expect fun isMobile(): Boolean

expect fun environmentVariable(name: String): String?

expect fun backendBaseUrl(): String

object RuntimeConfig {
    const val BACKEND_PORT_ENV = "SHOPMAP_BACKEND_PORT"
    const val OAUTH_CALLBACK_PORT_ENV = "SHOPMAP_OAUTH_CALLBACK_PORT"
    const val DEFAULT_BACKEND_PORT = 8082
    const val DEFAULT_OAUTH_CALLBACK_PORT = 8083
}

fun configuredPort(name: String, defaultPort: Int): Int {
    val value = environmentVariable(name)?.trim()
    if (value.isNullOrEmpty()) {
        return defaultPort
    }

    return value.toIntOrNull()?.takeIf { it in 1..65535 }
        ?: error("$name must be a port number between 1 and 65535, got '$value'.")
}
