package com.jammking.loopbox.adapter.out.persistence.jpa

import com.jammking.loopbox.domain.entity.auth.RefreshToken
import com.jammking.loopbox.domain.entity.user.User
import com.jammking.loopbox.domain.entity.user.UserId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import java.time.Instant

@DataJpaTest
@Import(
    JpaUserRepository::class,
    JpaRefreshTokenRepository::class
)
class JpaUserAndRefreshTokenRepositoryTest {

    @Autowired
    private lateinit var userRepository: JpaUserRepository

    @Autowired
    private lateinit var refreshTokenRepository: JpaRefreshTokenRepository

    @Test
    fun `user repository should save and find by email`() {
        val user = User(
            id = UserId("user-1"),
            email = "user@example.com",
            passwordHash = "hash",
            passwordSalt = "salt"
        )
        userRepository.save(user)

        val found = userRepository.findByEmail("user@example.com")

        assertEquals("user-1", found?.id?.value)
    }

    @Test
    fun `refresh token repository should delete by token and user id`() {
        val token = RefreshToken(
            token = "token-1",
            userId = UserId("user-1"),
            expiresAt = Instant.parse("2025-01-01T00:00:00Z")
        )
        refreshTokenRepository.save(token)

        assertEquals("token-1", refreshTokenRepository.findByToken("token-1")?.token)

        refreshTokenRepository.deleteByToken("token-1")
        assertNull(refreshTokenRepository.findByToken("token-1"))

        val token2 = RefreshToken(
            token = "token-2",
            userId = UserId("user-1"),
            expiresAt = Instant.parse("2025-01-01T00:00:00Z")
        )
        refreshTokenRepository.save(token2)
        refreshTokenRepository.deleteByUserId(UserId("user-1"))
        assertNull(refreshTokenRepository.findByToken("token-2"))
    }
}
