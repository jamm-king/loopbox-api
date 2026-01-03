package com.jammking.loopbox.domain.exception

abstract class ValidationException(
    override val errorCode: ErrorCode,
    override val message: String,
    override val cause: Throwable? = null
): LoopboxException(errorCode, ErrorCategory.VALIDATION, message, cause)