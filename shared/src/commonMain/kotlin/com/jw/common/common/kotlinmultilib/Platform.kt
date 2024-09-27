package com.jw.common.common.kotlinmultilib

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform