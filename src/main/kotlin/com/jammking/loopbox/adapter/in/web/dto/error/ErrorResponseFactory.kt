package com.jammking.loopbox.adapter.`in`.web.dto.error

import com.jammking.loopbox.domain.exception.ErrorCategory
import com.jammking.loopbox.domain.exception.ErrorCode
import com.jammking.loopbox.domain.exception.LoopboxException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.servlet.error.ErrorAttributes
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.context.request.ServletWebRequest

@Component
class ErrorResponseFactory(
    private val errorAttributes: ErrorAttributes
) {

    fun fromLoopbox(e: LoopboxException, request: HttpServletRequest): ErrorResponse =
        ErrorResponse(
            code = e.errorCode.name,
            category = e.errorCategory.name,
            message = e.message,
            path = request.requestURI,
            traceId = traceId(request),
            details = null
        )

    fun fromErrorAttributes(
        request: HttpServletRequest
    ): Pair<HttpStatus, ErrorResponse> {

        val webRequest = ServletWebRequest(request)
        val options = ErrorAttributeOptions.of(
            ErrorAttributeOptions.Include.MESSAGE
        )

        val attrs = errorAttributes.getErrorAttributes(webRequest, options)

        val status = (attrs["status"] as? Int)
            ?.let { HttpStatus.resolve(it) }
            ?: HttpStatus.INTERNAL_SERVER_ERROR

        val body = ErrorResponse(
            code = frameworkCode(status),
            category = ErrorCategory.SYSTEM.name,
            message = safeFrameworkMessage(status),
            path = (attrs["path"] as? String) ?: request.requestURI,
            traceId = traceId(request),
            details = frameworkDetails(attrs)
        )

        return status to body
    }

    private fun traceId(request: HttpServletRequest): String? =
        request.getAttribute("traceId") as String?

    private fun frameworkCode(status: HttpStatus): String =
        when (status) {
            HttpStatus.NOT_FOUND -> "ENDPOINT_NOT_FOUND"
            HttpStatus.METHOD_NOT_ALLOWED -> "METHOD_NOT_ALLOWED"
            HttpStatus.BAD_REQUEST -> "INVALID_REQUEST"
            HttpStatus.UNSUPPORTED_MEDIA_TYPE -> "UNSUPPORTED_MEDIA_TYPE"
            HttpStatus.NOT_ACCEPTABLE -> "NOT_ACCEPTABLE"
            HttpStatus.UNAUTHORIZED -> "UNAUTHORIZED"
            HttpStatus.FORBIDDEN -> "FORBIDDEN"
            HttpStatus.TOO_MANY_REQUESTS -> "RATE_LIMIT"
            else -> ErrorCode.INTERNAL_SERVER_ERROR.name
        }

    private fun safeFrameworkMessage(status: HttpStatus): String =
        when (status) {
            HttpStatus.NOT_FOUND -> "No such endpoint."
            HttpStatus.METHOD_NOT_ALLOWED -> "HTTP method not allowed."
            HttpStatus.BAD_REQUEST -> "Invalid request."
            HttpStatus.UNSUPPORTED_MEDIA_TYPE -> "Unsupported media type."
            HttpStatus.NOT_ACCEPTABLE -> "Not acceptable."
            HttpStatus.UNAUTHORIZED -> "Unauthorized."
            HttpStatus.FORBIDDEN -> "Forbidden."
            HttpStatus.TOO_MANY_REQUESTS -> "Too many requests."
            else -> "Unexpected error."
        }

    private fun frameworkDetails(attrs: Map<String, Any?>): Map<String, Any?>? {
        val timestamp = attrs["timestamp"]
        val error = attrs["error"]

        val details = buildMap {
            if (timestamp != null) put("timestamp", timestamp)
            if (error != null) put("error", error)
        }

        return details.ifEmpty { null }
    }
}