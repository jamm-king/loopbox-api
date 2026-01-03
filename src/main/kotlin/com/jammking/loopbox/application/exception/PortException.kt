package com.jammking.loopbox.application.exception

sealed class PortException(
    open val code: PortErrorCode,
    open val category: PortErrorCategory,
    override val message: String,
    override val cause: Throwable? = null
): RuntimeException(message, cause)