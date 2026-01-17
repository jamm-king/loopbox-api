package com.jammking.loopbox.application.service

import com.jammking.loopbox.adapter.out.persistence.jpa.JpaRefreshTokenRepository
import com.jammking.loopbox.adapter.out.persistence.jpa.JpaUserRepository
import com.jammking.loopbox.application.port.`in`.AuthUseCase
import com.jammking.loopbox.domain.exception.user.InvalidCredentialsException
import com.jammking.loopbox.domain.port.out.RefreshTokenRepository
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@DataJpaTest
@ActiveProfiles("postgresql")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@TestPropertySource(properties = ["loopbox.auth.jwt-secret=test-secret"])
@Import(
    AuthService::class,
    JwtTokenProvider::class,
    RefreshTokenCleanupService::class,
    JpaUserRepository::class,
    JpaRefreshTokenRepository::class
)
class AuthServiceTxTest {

    @Autowired
    private lateinit var authService: AuthService

    @Autowired
    private lateinit var refreshTokenRepository: RefreshTokenRepository

    @Autowired
    private lateinit var entityManager: EntityManager

    @Test
    fun `refresh removes expired token even when exception is thrown`() {
        val signup = authService.signup(
            AuthUseCase.SignupCommand(
                email = "expired@example.com",
                password = "password123"
            )
        )

        val stored = refreshTokenRepository.findByToken(signup.tokens.refreshToken)!!
        refreshTokenRepository.save(
            stored.copy(expiresAt = Instant.now().minusSeconds(60))
        )

        assertThrows(InvalidCredentialsException::class.java) {
            authService.refresh(
                AuthUseCase.RefreshCommand(
                    refreshToken = signup.tokens.refreshToken
                )
            )
        }

        entityManager.clear()
        assertNull(refreshTokenRepository.findByToken(signup.tokens.refreshToken))
    }
}
