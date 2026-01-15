package com.jammking.loopbox.domain.exception.user

import com.jammking.loopbox.domain.exception.ErrorCode
import com.jammking.loopbox.domain.exception.StateViolationException

class DuplicateUserEmailException(
    val email: String
): StateViolationException(
    errorCode = ErrorCode.DUPLICATE_USER_EMAIL,
    message = "State violation: Duplicate user email: email=$email"
)
