package com.jammking.loopbox.adapter.out.persistence.jpa.entity

import com.jammking.loopbox.domain.entity.user.User
import com.jammking.loopbox.domain.entity.user.UserId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "users")
class UserJpaEntity(
    @Id
    @Column(name = "id", nullable = false, length = 64)
    var id: String = "",
    @Column(name = "email", nullable = false, length = 255)
    var email: String = "",
    @Column(name = "password_hash", nullable = false, length = 255)
    var passwordHash: String = "",
    @Column(name = "password_salt", nullable = false, length = 255)
    var passwordSalt: String = "",
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.EPOCH,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.EPOCH
) {
    fun toDomain(): User = User(
        id = UserId(id),
        email = email,
        passwordHash = passwordHash,
        passwordSalt = passwordSalt,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(user: User): UserJpaEntity = UserJpaEntity(
            id = user.id.value,
            email = user.email,
            passwordHash = user.passwordHash,
            passwordSalt = user.passwordSalt,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt
        )
    }
}
