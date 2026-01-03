package com.jammking.loopbox.adapter.`in`.web.dto.error

data class ErrorResponse(
    val code: String,
    val category: String,
    val message: String,
    val path: String? = null,
    val traceId: String? = null,
    val details: Map<String, Any?>? = null
)