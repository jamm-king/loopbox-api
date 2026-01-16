package com.jammking.loopbox.adapter.out.persistence.jpa.entity

import com.jammking.loopbox.domain.entity.auth.RefreshToken
import com.jammking.loopbox.domain.entity.user.UserId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "refresh_tokens")
class RefreshTokenJpaEntity(
    @Id
    @Column(name = "token", nullable = false, columnDefinition = "text")
    var token: String = "",
    @Column(name = "user_id", nullable = false, length = 64)
    var userId: String = "",
    @Column(name = "expires_at", nullable = false)
    var expiresAt: Instant = Instant.EPOCH,
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.EPOCH
) {
    fun toDomain(): RefreshToken = RefreshToken(
        token = token,
        userId = UserId(userId),
        expiresAt = expiresAt,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(token: RefreshToken): RefreshTokenJpaEntity = RefreshTokenJpaEntity(
            token = token.token,
            userId = token.userId.value,
            expiresAt = token.expiresAt,
            createdAt = token.createdAt
        )
    }
}
