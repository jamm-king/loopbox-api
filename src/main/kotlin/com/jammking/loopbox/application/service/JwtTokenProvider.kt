package com.jammking.loopbox.application.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.jammking.loopbox.domain.entity.user.User
import com.jammking.loopbox.domain.entity.user.UserId
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Instant
import java.util.Base64
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Component
class JwtTokenProvider(
    @Value("\${loopbox.auth.jwt-secret:}") private val secret: String,
    @Value("\${loopbox.auth.access-token-ttl-seconds:900}") private val accessTokenTtlSeconds: Long,
    @Value("\${loopbox.auth.refresh-token-ttl-seconds:1209600}") private val refreshTokenTtlSeconds: Long,
    @Value("\${loopbox.auth.issuer:loopbox}") private val issuer: String,
    @Value("\${loopbox.auth.audience:loopbox-api}") private val audience: String
) {

    private val objectMapper = jacksonObjectMapper()
    private val encoder = Base64.getUrlEncoder().withoutPadding()
    private val decoder = Base64.getUrlDecoder()
    private val secretKey = SecretKeySpec(secret.toByteArray(StandardCharsets.UTF_8), HMAC_ALGORITHM)

    init {
        if (secret.isBlank()) {
            throw IllegalStateException("JWT secret is missing")
        }
    }

    data class TokenIssue(
        val token: String,
        val expiresAt: Instant
    )

    data class JwtClaims(
        val sub: String,
        val iss: String,
        val aud: String,
        val type: String,
        val jti: String,
        val iat: Long,
        val exp: Long
    )

    fun createAccessToken(user: User): TokenIssue =
        createToken(user, TOKEN_TYPE_ACCESS, accessTokenTtlSeconds)

    fun createRefreshToken(user: User): TokenIssue =
        createToken(user, TOKEN_TYPE_REFRESH, refreshTokenTtlSeconds)

    fun parseAndValidate(token: String, expectedType: String): JwtClaims {
        val parts = token.split(".")
        if (parts.size != 3) throw IllegalArgumentException("Invalid token format")

        val signature = sign("${parts[0]}.${parts[1]}")
        if (!MessageDigest.isEqual(signature, decoder.decode(parts[2]))) {
            throw IllegalArgumentException("Invalid token signature")
        }

        val payloadJson = String(decoder.decode(parts[1]), StandardCharsets.UTF_8)
        val claims: JwtClaims = objectMapper.readValue(payloadJson)
        if (claims.iss != issuer || claims.aud != audience) {
            throw IllegalArgumentException("Invalid token issuer or audience")
        }
        if (claims.type != expectedType) {
            throw IllegalArgumentException("Invalid token type")
        }
        val now = Instant.now().epochSecond
        if (claims.exp <= now) {
            throw IllegalArgumentException("Token expired")
        }
        return claims
    }

    fun toUserId(claims: JwtClaims): UserId =
        UserId(claims.sub)

    private fun createToken(user: User, type: String, ttlSeconds: Long): TokenIssue {
        val now = Instant.now()
        val exp = now.plusSeconds(ttlSeconds)
        val headerJson = objectMapper.writeValueAsBytes(JWT_HEADER)
        val payloadJson = objectMapper.writeValueAsBytes(
            JwtClaims(
                sub = user.id.value,
                iss = issuer,
                aud = audience,
                type = type,
                jti = UUID.randomUUID().toString(),
                iat = now.epochSecond,
                exp = exp.epochSecond
            )
        )
        val header = encoder.encodeToString(headerJson)
        val payload = encoder.encodeToString(payloadJson)
        val signature = encoder.encodeToString(sign("$header.$payload"))

        return TokenIssue(
            token = "$header.$payload.$signature",
            expiresAt = exp
        )
    }

    private fun sign(input: String): ByteArray {
        val mac = Mac.getInstance(HMAC_ALGORITHM)
        mac.init(secretKey)
        return mac.doFinal(input.toByteArray(StandardCharsets.UTF_8))
    }

    companion object {
        private const val HMAC_ALGORITHM = "HmacSHA256"
        private const val TOKEN_TYPE_ACCESS = "access"
        private const val TOKEN_TYPE_REFRESH = "refresh"
        private val JWT_HEADER = mapOf(
            "alg" to "HS256",
            "typ" to "JWT"
        )
    }
}
