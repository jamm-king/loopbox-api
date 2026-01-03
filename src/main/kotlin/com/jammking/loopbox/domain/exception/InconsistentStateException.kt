package com.jammking.loopbox.domain.exception

abstract class InconsistentStateException(
    override val errorCode: ErrorCode,
    override val message: String,
    override val cause: Throwable? = null
): LoopboxException(errorCode, ErrorCategory.INCONSISTENT_STATE, message, cause)