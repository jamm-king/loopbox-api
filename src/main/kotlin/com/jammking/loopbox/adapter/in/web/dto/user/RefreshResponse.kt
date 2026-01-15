package com.jammking.loopbox.adapter.`in`.web.dto.user

data class RefreshResponse(
    val user: WebUser,
    val accessToken: String,
    val refreshToken: String
)
