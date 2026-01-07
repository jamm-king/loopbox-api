package com.jammking.loopbox.adapter.`in`.web.dto.image

data class GenerateImageVersionRequest(
    val provider: String,
    val description: String? = null,
    val width: Int? = null,
    val height: Int? = null
)
