package com.jammking.loopbox.adapter.`in`.web.mapper

import com.jammking.loopbox.application.exception.PortErrorCode
import com.jammking.loopbox.domain.exception.ErrorCode
import org.springframework.http.HttpStatus

object HttpStatusMapper {

    fun ErrorCode.toHttpStatus(): HttpStatus = when (this) {
        ErrorCode.PROJECT_NOT_FOUND -> HttpStatus.NOT_FOUND
        ErrorCode.MUSIC_NOT_FOUND -> HttpStatus.NOT_FOUND
        ErrorCode.IMAGE_NOT_FOUND -> HttpStatus.NOT_FOUND
        ErrorCode.VIDEO_NOT_FOUND -> HttpStatus.NOT_FOUND
        ErrorCode.VERSION_NOT_FOUND -> HttpStatus.NOT_FOUND
        ErrorCode.TASK_NOT_FOUND -> HttpStatus.NOT_FOUND
        ErrorCode.RESOURCE_NOT_FOUND -> HttpStatus.NOT_FOUND

        ErrorCode.INCONSISTENT_PROJECT_MUSIC_RELATION -> HttpStatus.CONFLICT
        ErrorCode.INCONSISTENT_PROJECT_IMAGE_RELATION -> HttpStatus.CONFLICT
        ErrorCode.INCONSISTENT_MUSIC_TASK_RELATION -> HttpStatus.CONFLICT
        ErrorCode.INCONSISTENT_IMAGE_TASK_RELATION -> HttpStatus.CONFLICT

        ErrorCode.INVALID_PROJECT_STATE -> HttpStatus.CONFLICT
        ErrorCode.INVALID_MUSIC_STATE -> HttpStatus.CONFLICT
        ErrorCode.INVALID_MUSIC_VERSION_STATE -> HttpStatus.CONFLICT
        ErrorCode.INVALID_IMAGE_STATE -> HttpStatus.CONFLICT
        ErrorCode.INVALID_IMAGE_VERSION_STATE -> HttpStatus.CONFLICT
        ErrorCode.INVALID_VIDEO_STATE -> HttpStatus.CONFLICT
        ErrorCode.INVALID_TASK_STATE -> HttpStatus.CONFLICT
        ErrorCode.DUPLICATE_PROJECT_ID -> HttpStatus.CONFLICT
        ErrorCode.DUPLICATE_MUSIC_ID -> HttpStatus.CONFLICT
        ErrorCode.DUPLICATE_VERSION_ID -> HttpStatus.CONFLICT

        ErrorCode.INVALID_PROJECT_TITLE -> HttpStatus.BAD_REQUEST
        ErrorCode.INVALID_MUSIC_AI_PROVIDER -> HttpStatus.BAD_REQUEST
        ErrorCode.INVALID_AUDIO_FILE_PATH -> HttpStatus.BAD_REQUEST
        ErrorCode.INVALID_IMAGE_AI_PROVIDER -> HttpStatus.BAD_REQUEST
        ErrorCode.INVALID_IMAGE_FILE_PATH -> HttpStatus.BAD_REQUEST
        ErrorCode.INVALID_VIDEO_EDIT -> HttpStatus.BAD_REQUEST

        ErrorCode.INTERNAL_SERVER_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR
    }

    fun PortErrorCode.toHttpStatus(): HttpStatus = when (this) {
        PortErrorCode.UNAUTHORIZED -> HttpStatus.UNAUTHORIZED
        PortErrorCode.FORBIDDEN -> HttpStatus.FORBIDDEN
        PortErrorCode.NOT_FOUND -> HttpStatus.BAD_GATEWAY   // or 502
        PortErrorCode.RATE_LIMITED -> HttpStatus.TOO_MANY_REQUESTS
        PortErrorCode.QUOTA_EXCEEDED -> HttpStatus.PAYMENT_REQUIRED
        PortErrorCode.INVALID_REQUEST -> HttpStatus.BAD_REQUEST
        PortErrorCode.PROTOCOL_VIOLATION -> HttpStatus.BAD_GATEWAY
        PortErrorCode.TEMPORARY_UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE
        PortErrorCode.UNKNOWN -> HttpStatus.BAD_GATEWAY
    }
}
