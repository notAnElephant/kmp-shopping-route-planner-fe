package org.example.srpfe.utils

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.posix.getenv

actual fun isMobile(): Boolean = true

@OptIn(ExperimentalForeignApi::class)
actual fun environmentVariable(name: String): String? = getenv(name)?.toKString()

actual fun backendBaseUrl(): String =
    "http://127.0.0.1:${configuredPort(RuntimeConfig.BACKEND_PORT_ENV, RuntimeConfig.DEFAULT_BACKEND_PORT)}"
