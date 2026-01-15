package com.jammking.loopbox.application.service

import com.jammking.loopbox.application.port.`in`.AuthUseCase
import com.jammking.loopbox.adapter.out.persistence.inmemeory.InMemoryRefreshTokenRepository
import com.jammking.loopbox.adapter.out.persistence.inmemeory.InMemoryUserRepository
import com.jammking.loopbox.domain.exception.user.DuplicateUserEmailException
import com.jammking.loopbox.domain.exception.user.InvalidCredentialsException
import com.jammking.loopbox.domain.exception.user.InvalidUserPasswordException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class AuthServiceTest {

    private val userRepository = InMemoryUserRepository()
    private val refreshTokenRepository = InMemoryRefreshTokenRepository()
    private val jwtTokenProvider = JwtTokenProvider(
        secret = "test-secret",
        accessTokenTtlSeconds = 60,
        refreshTokenTtlSeconds = 120,
        issuer = "loopbox-test",
        audience = "loopbox-api-test"
    )
    private val authService = AuthService(userRepository, refreshTokenRepository, jwtTokenProvider)

    @Test
    fun `signup should normalize email and hash password`() {
        // Given
        val command = AuthUseCase.SignupCommand(
            email = "  TEST@EXAMPLE.COM  ",
            password = "password123"
        )

        // When
        val result = authService.signup(command)

        // Then
        assertEquals("test@example.com", result.user.email)
        val stored = userRepository.findByEmail("test@example.com")
        assertNotNull(stored)
        assertNotEquals("password123", stored!!.passwordHash)
        assertNotEquals("", stored.passwordSalt)
        assertNotEquals("", result.tokens.accessToken)
        assertNotEquals("", result.tokens.refreshToken)
    }

    @Test
    fun `signup should reject duplicate email`() {
        // Given
        authService.signup(
            AuthUseCase.SignupCommand(
                email = "user@example.com",
                password = "password123"
            )
        )

        // When & Then
        assertThrows(DuplicateUserEmailException::class.java) {
            authService.signup(
                AuthUseCase.SignupCommand(
                    email = "User@Example.com",
                    password = "password123"
                )
            )
        }
    }

    @Test
    fun `signup should reject weak password`() {
        // When & Then
        assertThrows(InvalidUserPasswordException::class.java) {
            authService.signup(
                AuthUseCase.SignupCommand(
                    email = "user2@example.com",
                    password = "short"
                )
            )
        }
    }

    @Test
    fun `login should return user with correct password`() {
        // Given
        val user = authService.signup(
            AuthUseCase.SignupCommand(
                email = "login@example.com",
                password = "password123"
            )
        )

        // When
        val result = authService.login(
            AuthUseCase.LoginCommand(
                email = "login@example.com",
                password = "password123"
            )
        )

        // Then
        assertEquals(user.user.id.value, result.user.id.value)
    }

    @Test
    fun `login should reject invalid password`() {
        // Given
        authService.signup(
            AuthUseCase.SignupCommand(
                email = "login2@example.com",
                password = "password123"
            )
        )

        // When & Then
        assertThrows(InvalidCredentialsException::class.java) {
            authService.login(
                AuthUseCase.LoginCommand(
                    email = "login2@example.com",
                    password = "wrong-password"
                )
            )
        }
    }

    @Test
    fun `refresh should rotate refresh token`() {
        // Given
        val signup = authService.signup(
            AuthUseCase.SignupCommand(
                email = "refresh@example.com",
                password = "password123"
            )
        )

        // When
        val refreshed = authService.refresh(
            AuthUseCase.RefreshCommand(
                refreshToken = signup.tokens.refreshToken
            )
        )

        // Then
        assertEquals(signup.user.id.value, refreshed.user.id.value)
        assertNotEquals(signup.tokens.refreshToken, refreshed.tokens.refreshToken)
        assertNotEquals("", refreshed.tokens.accessToken)
    }

    @Test
    fun `logout should revoke refresh token`() {
        // Given
        val signup = authService.signup(
            AuthUseCase.SignupCommand(
                email = "logout@example.com",
                password = "password123"
            )
        )

        // When
        authService.logout(
            AuthUseCase.LogoutCommand(
                refreshToken = signup.tokens.refreshToken
            )
        )

        // Then
        assertThrows(InvalidCredentialsException::class.java) {
            authService.refresh(
                AuthUseCase.RefreshCommand(
                    refreshToken = signup.tokens.refreshToken
                )
            )
        }
    }
}
