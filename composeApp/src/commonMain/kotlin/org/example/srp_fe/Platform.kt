package org.example.srp_fe

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform