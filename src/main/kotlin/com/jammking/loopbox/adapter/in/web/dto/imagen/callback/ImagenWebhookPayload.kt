package com.jammking.loopbox.adapter.`in`.web.dto.imagen.callback

import com.fasterxml.jackson.annotation.JsonProperty

data class ImagenWebhookPayload(
    val id: String? = null,
    val status: String? = null,
    val output: String? = null,
    val error: String? = null,
    @JsonProperty("created_at")
    val createdAt: String? = null,
    @JsonProperty("completed_at")
    val completedAt: String? = null
)
