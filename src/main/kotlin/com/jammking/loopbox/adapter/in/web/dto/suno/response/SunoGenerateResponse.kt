package com.jammking.loopbox.adapter.`in`.web.dto.suno.response

data class SunoGenerateResponse(
    val code: Int,
    val msg: String,
    val data: SunoGenerateData?
)