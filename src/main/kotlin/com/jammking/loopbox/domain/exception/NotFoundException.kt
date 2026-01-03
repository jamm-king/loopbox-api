package com.jammking.loopbox.domain.exception

abstract class NotFoundException(
    override val errorCode: ErrorCode,
    override val message: String,
    override val cause: Throwable? = null
): LoopboxException(errorCode, ErrorCategory.NOT_FOUND, message, cause)
