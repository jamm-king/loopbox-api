package com.jammking.loopbox.adapter.`in`.web.support

import com.jammking.loopbox.application.service.JwtTokenProvider
import com.jammking.loopbox.domain.entity.user.User
import com.jammking.loopbox.domain.entity.user.UserId
import com.jammking.loopbox.domain.exception.UnauthorizedException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class AuthenticatedUserResolverTest {

    private val jwtTokenProvider = JwtTokenProvider(
        secret = "test-secret",
        accessTokenTtlSeconds = 900,
        refreshTokenTtlSeconds = 1209600,
        issuer = "loopbox",
        audience = "loopbox-api"
    )
    private val resolver = AuthenticatedUserResolver(jwtTokenProvider)

    @Test
    fun `resolve should parse bearer token from header`() {
        val user = User(
            id = UserId("user-1"),
            email = "user@example.com",
            passwordHash = "hash",
            passwordSalt = "salt"
        )
        val token = jwtTokenProvider.createAccessToken(user).token

        val resolved = resolver.resolve("Bearer $token", null)

        assertEquals(user.id, resolved)
    }

    @Test
    fun `resolve should accept access token param`() {
        val user = User(
            id = UserId("user-2"),
            email = "user2@example.com",
            passwordHash = "hash",
            passwordSalt = "salt"
        )
        val token = jwtTokenProvider.createAccessToken(user).token

        val resolved = resolver.resolve(null, token)

        assertEquals(user.id, resolved)
    }

    @Test
    fun `resolve should reject invalid header`() {
        assertThrows(UnauthorizedException::class.java) {
            resolver.resolve("Token abc", null)
        }
    }

    @Test
    fun `resolve should reject invalid token`() {
        assertThrows(UnauthorizedException::class.java) {
            resolver.resolve("Bearer invalid", null)
        }
    }
}
