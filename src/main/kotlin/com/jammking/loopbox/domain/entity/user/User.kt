package com.jammking.loopbox.domain.entity.user

import com.jammking.loopbox.domain.exception.user.InvalidUserEmailException
import java.time.Instant
import java.util.UUID

class User(
    val id: UserId = UserId(UUID.randomUUID().toString()),
    email: String,
    passwordHash: String,
    passwordSalt: String,
    val createdAt: Instant = Instant.now(),
    updatedAt: Instant = Instant.now()
) {

    init {
        if (!validateEmail(email)) throw InvalidUserEmailException(email)
    }

    var email: String = email
        private set

    var passwordHash: String = passwordHash
        private set

    var passwordSalt: String = passwordSalt
        private set

    var updatedAt: Instant = updatedAt
        private set

    fun updatePassword(
        newHash: String,
        newSalt: String,
        now: Instant = Instant.now()
    ) {
        passwordHash = newHash
        passwordSalt = newSalt
        updatedAt = now
    }

    fun copy(
        id: UserId = this.id,
        email: String = this.email,
        passwordHash: String = this.passwordHash,
        passwordSalt: String = this.passwordSalt,
        createdAt: Instant = this.createdAt,
        updatedAt: Instant = this.updatedAt
    ): User = User(
        id = id,
        email = email,
        passwordHash = passwordHash,
        passwordSalt = passwordSalt,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun validateEmail(email: String): Boolean {
        if (email.isBlank()) return false
        return email.contains("@")
    }
}
