package com.jammking.loopbox.domain.exception

class UnauthorizedException(
    override val message: String = "Unauthorized.",
    override val cause: Throwable? = null
) : ValidationException(ErrorCode.UNAUTHORIZED, message, cause)
