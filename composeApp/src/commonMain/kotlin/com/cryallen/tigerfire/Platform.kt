package com.cryallen.tigerfire

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform