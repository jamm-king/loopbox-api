package com.jammking.loopbox.application.service

import com.jammking.loopbox.application.port.`in`.AuthUseCase
import com.jammking.loopbox.domain.entity.auth.RefreshToken
import com.jammking.loopbox.domain.entity.user.User
import com.jammking.loopbox.domain.exception.user.DuplicateUserEmailException
import com.jammking.loopbox.domain.exception.user.InvalidCredentialsException
import com.jammking.loopbox.domain.exception.user.InvalidUserPasswordException
import com.jammking.loopbox.domain.port.out.RefreshTokenRepository
import com.jammking.loopbox.domain.port.out.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val refreshTokenCleanupService: RefreshTokenCleanupService,
    private val jwtTokenProvider: JwtTokenProvider
): AuthUseCase {

    private val log = LoggerFactory.getLogger(javaClass)
    private val secureRandom = SecureRandom()

    @Transactional
    override fun signup(command: AuthUseCase.SignupCommand): AuthUseCase.AuthResult {
        val normalizedEmail = normalizeEmail(command.email)
        validatePassword(command.password)

        val existing = userRepository.findByEmail(normalizedEmail)
        if (existing != null) {
            throw DuplicateUserEmailException(normalizedEmail)
        }

        val salt = generateSalt()
        val passwordHash = hashPassword(command.password, salt)
        val user = User(
            email = normalizedEmail,
            passwordHash = passwordHash,
            passwordSalt = encodeBase64(salt)
        )
        val saved = userRepository.save(user)
        log.info("Signed up user: userId={}, email={}", saved.id.value, saved.email)

        val tokens = issueTokens(saved)
        return AuthUseCase.AuthResult(saved, tokens)
    }

    @Transactional
    override fun login(command: AuthUseCase.LoginCommand): AuthUseCase.AuthResult {
        val normalizedEmail = normalizeEmail(command.email)
        val user = userRepository.findByEmail(normalizedEmail)
            ?: throw InvalidCredentialsException()

        val salt = decodeBase64(user.passwordSalt)
        val hashedInput = hashPassword(command.password, salt)
        if (hashedInput != user.passwordHash) {
            throw InvalidCredentialsException()
        }

        log.info("User login: userId={}, email={}", user.id.value, user.email)
        val tokens = issueTokens(user)
        return AuthUseCase.AuthResult(user, tokens)
    }

    @Transactional
    override fun refresh(command: AuthUseCase.RefreshCommand): AuthUseCase.AuthResult {
        val claims = try {
            jwtTokenProvider.parseAndValidate(command.refreshToken, "refresh")
        } catch (e: Exception) {
            throw InvalidCredentialsException()
        }

        val stored = refreshTokenRepository.findByToken(command.refreshToken)
            ?: throw InvalidCredentialsException()
        if (stored.expiresAt.isBefore(java.time.Instant.now())) {
            refreshTokenCleanupService.deleteByToken(command.refreshToken)
            throw InvalidCredentialsException()
        }

        val userId = jwtTokenProvider.toUserId(claims)
        val user = userRepository.findById(userId)
            ?: throw InvalidCredentialsException()

        refreshTokenRepository.deleteByToken(command.refreshToken)
        val tokens = issueTokens(user)
        return AuthUseCase.AuthResult(user, tokens)
    }

    @Transactional
    override fun logout(command: AuthUseCase.LogoutCommand) {
        val claims = try {
            jwtTokenProvider.parseAndValidate(command.refreshToken, "refresh")
        } catch (e: Exception) {
            throw InvalidCredentialsException()
        }

        val stored = refreshTokenRepository.findByToken(command.refreshToken)
            ?: throw InvalidCredentialsException()

        val userId = jwtTokenProvider.toUserId(claims)
        if (stored.userId != userId) {
            throw InvalidCredentialsException()
        }

        refreshTokenRepository.deleteByToken(command.refreshToken)
    }

    private fun normalizeEmail(email: String): String =
        email.trim().lowercase()

    private fun validatePassword(password: String) {
        if (password.length < MIN_PASSWORD_LENGTH) {
            throw InvalidUserPasswordException("password must be at least $MIN_PASSWORD_LENGTH characters")
        }
    }

    private fun generateSalt(): ByteArray {
        val salt = ByteArray(SALT_SIZE)
        secureRandom.nextBytes(salt)
        return salt
    }

    private fun hashPassword(password: String, salt: ByteArray): String {
        val spec = PBEKeySpec(password.toCharArray(), salt, HASH_ITERATIONS, HASH_KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance(HASH_ALGORITHM)
        val hash = factory.generateSecret(spec).encoded
        spec.clearPassword()
        return encodeBase64(hash)
    }

    private fun encodeBase64(value: ByteArray): String =
        Base64.getEncoder().encodeToString(value)

    private fun decodeBase64(value: String): ByteArray =
        Base64.getDecoder().decode(value)

    private fun issueTokens(user: User): AuthUseCase.AuthTokens {
        refreshTokenRepository.deleteByUserId(user.id)
        val access = jwtTokenProvider.createAccessToken(user)
        val refresh = jwtTokenProvider.createRefreshToken(user)
        refreshTokenRepository.save(
            RefreshToken(
                token = refresh.token,
                userId = user.id,
                expiresAt = refresh.expiresAt
            )
        )
        return AuthUseCase.AuthTokens(
            accessToken = access.token,
            refreshToken = refresh.token
        )
    }

    companion object {
        private const val MIN_PASSWORD_LENGTH = 8
        private const val SALT_SIZE = 16
        private const val HASH_ITERATIONS = 10000
        private const val HASH_KEY_LENGTH = 256
        private const val HASH_ALGORITHM = "PBKDF2WithHmacSHA256"
    }
}
