package org.example.srpfe.utils

actual fun isMobile(): Boolean = false

actual fun environmentVariable(name: String): String? = System.getenv(name)

actual fun backendBaseUrl(): String =
    "http://127.0.0.1:${configuredPort(RuntimeConfig.BACKEND_PORT_ENV, RuntimeConfig.DEFAULT_BACKEND_PORT)}"
