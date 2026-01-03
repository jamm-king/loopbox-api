package com.jammking.loopbox.domain.exception

abstract class LoopboxException(
    open val errorCode: ErrorCode,
    open val errorCategory: ErrorCategory,
    override val message: String,
    override val cause: Throwable? = null
): RuntimeException(message, cause)