package com.jammking.loopbox.domain.exception

abstract class StateViolationException(
    override val errorCode: ErrorCode,
    override val message: String,
    override val cause: Throwable? = null
): LoopboxException(errorCode, ErrorCategory.STATE_VIOLATION, message, cause)