package org.example.srpfe.utils

actual fun isMobile(): Boolean = true

actual fun environmentVariable(name: String): String? = System.getenv(name)

actual fun backendBaseUrl(): String =
    "http://10.0.2.2:${configuredPort(RuntimeConfig.BACKEND_PORT_ENV, RuntimeConfig.DEFAULT_BACKEND_PORT)}"
