package com.jammking.loopbox.adapter.`in`.web.exception

import org.springframework.http.HttpStatus

enum class WebErrorCode(val status: HttpStatus) {
    ENDPOINT_NOT_FOUND(HttpStatus.NOT_FOUND),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    NOT_ACCEPTABLE(HttpStatus.NOT_ACCEPTABLE),
    INVALID_REQUEST_BODY(HttpStatus.BAD_REQUEST),
    INVALID_ARGUMENT(HttpStatus.BAD_REQUEST),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST)
}