package com.jammking.loopbox.adapter.`in`.web.controller

import com.jammking.loopbox.adapter.`in`.web.dto.user.LoginRequest
import com.jammking.loopbox.adapter.`in`.web.dto.user.LoginResponse
import com.jammking.loopbox.adapter.`in`.web.dto.user.LogoutRequest
import com.jammking.loopbox.adapter.`in`.web.dto.user.RefreshRequest
import com.jammking.loopbox.adapter.`in`.web.dto.user.RefreshResponse
import com.jammking.loopbox.adapter.`in`.web.dto.user.SignupRequest
import com.jammking.loopbox.adapter.`in`.web.dto.user.SignupResponse
import com.jammking.loopbox.adapter.`in`.web.mapper.WebUserMapper.toWeb
import com.jammking.loopbox.application.port.`in`.AuthUseCase
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authUseCase: AuthUseCase
) {
    @PostMapping("/signup")
    fun signup(
        @RequestBody request: SignupRequest
    ): SignupResponse {
        val result = authUseCase.signup(
            AuthUseCase.SignupCommand(
                email = request.email,
                password = request.password
            )
        )
        return SignupResponse(
            user = result.user.toWeb(),
            accessToken = result.tokens.accessToken,
            refreshToken = result.tokens.refreshToken
        )
    }

    @PostMapping("/login")
    fun login(
        @RequestBody request: LoginRequest
    ): LoginResponse {
        val result = authUseCase.login(
            AuthUseCase.LoginCommand(
                email = request.email,
                password = request.password
            )
        )
        return LoginResponse(
            user = result.user.toWeb(),
            accessToken = result.tokens.accessToken,
            refreshToken = result.tokens.refreshToken
        )
    }

    @PostMapping("/refresh")
    fun refresh(
        @RequestBody request: RefreshRequest
    ): RefreshResponse {
        val result = authUseCase.refresh(
            AuthUseCase.RefreshCommand(
                refreshToken = request.refreshToken
            )
        )
        return RefreshResponse(
            user = result.user.toWeb(),
            accessToken = result.tokens.accessToken,
            refreshToken = result.tokens.refreshToken
        )
    }

    @PostMapping("/logout")
    fun logout(
        @RequestBody request: LogoutRequest
    ) {
        authUseCase.logout(
            AuthUseCase.LogoutCommand(
                refreshToken = request.refreshToken
            )
        )
    }
}
