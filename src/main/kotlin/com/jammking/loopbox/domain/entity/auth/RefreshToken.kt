package com.jammking.loopbox.domain.entity.auth

import com.jammking.loopbox.domain.entity.user.UserId
import java.time.Instant

data class RefreshToken(
    val token: String,
    val userId: UserId,
    val expiresAt: Instant,
    val createdAt: Instant = Instant.now()
)
