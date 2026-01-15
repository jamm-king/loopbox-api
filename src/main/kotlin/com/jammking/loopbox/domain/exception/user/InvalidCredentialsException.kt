package com.jammking.loopbox.domain.exception.user

import com.jammking.loopbox.domain.exception.ErrorCode
import com.jammking.loopbox.domain.exception.ValidationException

class InvalidCredentialsException: ValidationException(
    errorCode = ErrorCode.INVALID_CREDENTIALS,
    message = "Validation: Invalid login credentials"
)
