package com.jammking.loopbox.adapter.`in`.web.dto.image

data class WebImageVersion(
    val id: String,
    val fileId: String? = null,
    val url: String? = null,
    val config: WebImageConfig
)
