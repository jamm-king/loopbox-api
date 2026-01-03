package com.jammking.loopbox.application.exception

enum class PortErrorCode {
    TEMPORARY_UNAVAILABLE,
    UNAUTHORIZED,
    FORBIDDEN,
    NOT_FOUND,
    RATE_LIMITED,
    INVALID_REQUEST,
    PROTOCOL_VIOLATION,
    QUOTA_EXCEEDED,
    UNKNOWN
}