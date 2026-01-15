package com.jammking.loopbox.domain.exception.user

import com.jammking.loopbox.domain.exception.ErrorCode
import com.jammking.loopbox.domain.exception.ValidationException

class InvalidUserEmailException(
    val email: String
): ValidationException(
    errorCode = ErrorCode.INVALID_USER_EMAIL,
    message = "Validation: Invalid user email: email=$email"
)
