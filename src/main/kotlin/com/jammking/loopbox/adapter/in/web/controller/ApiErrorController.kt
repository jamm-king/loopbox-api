package com.jammking.loopbox.adapter.`in`.web.controller

import com.jammking.loopbox.adapter.`in`.web.dto.error.ErrorResponse
import com.jammking.loopbox.adapter.`in`.web.dto.error.ErrorResponseFactory
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.boot.web.servlet.error.ErrorAttributes
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.ServletWebRequest

@RestController
class ApiErrorController(
    private val factory: ErrorResponseFactory,
    private val errorAttributes: ErrorAttributes
): ErrorController {

    private val log = LoggerFactory.getLogger(javaClass)

    @RequestMapping("/error")
    fun handleError(request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val (status, body) = factory.fromErrorAttributes(request)

        val webRequest = ServletWebRequest(request)
        val cause: Throwable? = errorAttributes.getError(webRequest)

        val method = request.method
        val path = request.requestURI
        val traceId = (request.getAttribute("traceId") as? String)
            ?: request.getHeader("X-Trace-Id")

        when {
            status.is5xxServerError -> {
                log.error(
                    "Unhandled error: status={}, method={}, path={}, traceId={}, code={}",
                    status.value(), method, path, traceId, body.code,
                    cause
                )
            }
            status == HttpStatus.NOT_FOUND -> {
                log.info(
                    "Request not found: status={}, method={}, path={}, traceId={}",
                    status.value(), method, path, traceId
                )
            }
            else -> {
                log.warn(
                    "Client error: status={}, method={}, path={}, traceId={}, code={}",
                    status.value(), method, path, traceId, body.code
                )
            }
        }

        return ResponseEntity.status(status).body(body)
    }
}