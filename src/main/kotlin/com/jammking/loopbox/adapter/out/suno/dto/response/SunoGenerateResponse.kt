package com.jammking.loopbox.adapter.out.suno.dto.response

data class SunoGenerateResponse(
    val code: Int,
    val msg: String,
    val data: SunoGenerateData?
)
