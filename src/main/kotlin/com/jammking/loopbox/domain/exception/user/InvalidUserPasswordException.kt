package com.jammking.loopbox.domain.exception.user

import com.jammking.loopbox.domain.exception.ErrorCode
import com.jammking.loopbox.domain.exception.ValidationException

class InvalidUserPasswordException(
    val reason: String
): ValidationException(
    errorCode = ErrorCode.INVALID_USER_PASSWORD,
    message = "Validation: Invalid user password: reason=$reason"
)
