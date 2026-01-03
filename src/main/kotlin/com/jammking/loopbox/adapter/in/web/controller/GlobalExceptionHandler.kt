package com.jammking.loopbox.adapter.`in`.web.controller

import com.jammking.loopbox.adapter.`in`.web.dto.error.ErrorResponse
import com.jammking.loopbox.adapter.`in`.web.dto.error.ErrorResponseFactory
import com.jammking.loopbox.adapter.`in`.web.mapper.HttpStatusMapper.toHttpStatus
import com.jammking.loopbox.application.exception.PortException
import com.jammking.loopbox.domain.exception.LoopboxException
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler(
    private val factory: ErrorResponseFactory
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(LoopboxException::class)
    fun handleLoopboxException(e: LoopboxException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val traceId = getTraceId(request)
        val status = e.errorCode.toHttpStatus()
        
        log.warn(
            "Loopbox error: code={}, category={}, status={}, method={}, path={}, traceId={}, message={}",
            e.errorCode.name,
            e.errorCategory.name,
            status.value(),
            request.method,
            request.requestURI,
            traceId,
            e.message
        )

        return ResponseEntity
            .status(status)
            .body(factory.fromLoopbox(e, request))
    }

    @ExceptionHandler(PortException::class)
    fun handlePortException(e: PortException, request: HttpServletRequest) {
        val traceId = getTraceId(request)
        val status = e.code.toHttpStatus()
        if (status.is5xxServerError) {
            log.error(
                "Port error: code={}, category={}, status={}, method={}, path={}, traceId={}, message={}",
                e.code.name,
                e.category.name,
                status.value(),
                request.method,
                request.requestURI,
                traceId,
                e.message,
                e.cause
            )
        } else {
            log.warn(
                "Port error: code={}, category={}, status={}, method={}, path={}, traceId={}, message={}",
                e.code.name,
                e.category.name,
                status.value(),
                request.method,
                request.requestURI,
                traceId,
                e.message
            )
        }
    }

    private fun getTraceId(request: HttpServletRequest): String? {
        return (request.getAttribute("traceId") as? String)
            ?: request.getHeader("X-Trace-Id")
            ?: MDC.get("traceId")
    }
}