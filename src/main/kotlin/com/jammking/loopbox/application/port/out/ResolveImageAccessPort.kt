package com.jammking.loopbox.application.port.out

import java.time.Instant

interface ResolveImageAccessPort {
    fun resolve(path: String): ImageAccessTarget

    data class ImageAccessTarget(
        val url: String,
        val expiresAt: Instant? = null
    )
}
