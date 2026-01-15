package com.jammking.loopbox.adapter.`in`.web.support

import com.jammking.loopbox.application.service.JwtTokenProvider
import com.jammking.loopbox.domain.entity.user.UserId
import com.jammking.loopbox.domain.exception.UnauthorizedException
import org.springframework.stereotype.Component

@Component
class AuthenticatedUserResolver(
    private val jwtTokenProvider: JwtTokenProvider
) {

    fun resolve(authorization: String?, accessToken: String? = null): UserId {
        val token = when {
            !accessToken.isNullOrBlank() -> accessToken
            !authorization.isNullOrBlank() -> extractBearer(authorization)
            else -> throw UnauthorizedException("Missing access token.")
        }

        val claims = try {
            jwtTokenProvider.parseAndValidate(token, "access")
        } catch (e: IllegalArgumentException) {
            throw UnauthorizedException("Invalid access token.", e)
        }

        return jwtTokenProvider.toUserId(claims)
    }

    private fun extractBearer(header: String): String {
        val trimmed = header.trim()
        if (!trimmed.startsWith(BEARER_PREFIX, ignoreCase = true)) {
            throw UnauthorizedException("Invalid authorization header.")
        }
        val token = trimmed.substring(BEARER_PREFIX.length).trim()
        if (token.isBlank()) {
            throw UnauthorizedException("Invalid authorization header.")
        }
        return token
    }

    private companion object {
        private const val BEARER_PREFIX = "Bearer "
    }
}
