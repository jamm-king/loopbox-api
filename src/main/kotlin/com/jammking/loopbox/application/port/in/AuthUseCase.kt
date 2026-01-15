package com.jammking.loopbox.application.port.`in`

import com.jammking.loopbox.domain.entity.user.User

interface AuthUseCase {

    data class AuthTokens(
        val accessToken: String,
        val refreshToken: String
    )

    data class AuthResult(
        val user: User,
        val tokens: AuthTokens
    )

    data class SignupCommand(
        val email: String,
        val password: String
    )

    data class LoginCommand(
        val email: String,
        val password: String
    )

    data class RefreshCommand(
        val refreshToken: String
    )

    data class LogoutCommand(
        val refreshToken: String
    )

    fun signup(command: SignupCommand): AuthResult
    fun login(command: LoginCommand): AuthResult
    fun refresh(command: RefreshCommand): AuthResult
    fun logout(command: LogoutCommand)
}
