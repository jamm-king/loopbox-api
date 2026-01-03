package com.jammking.loopbox.adapter.`in`.web.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class TraceIdFilter: OncePerRequestFilter() {

    companion object {
        const val TRACE_HEADER = "X-Trace-Id"
        const val REQUEST_ATTR = "traceId"
        const val MDC_KEY = "traceId"

        private const val MAX_LEN = 128
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return false
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val incoming = request.getHeader(TRACE_HEADER)
        val normalized = normalize(incoming)
        val traceId = if(isValid(normalized)) normalized else newTraceId()

        request.setAttribute(REQUEST_ATTR, traceId)
        MDC.put(MDC_KEY, traceId)
        response.setHeader(TRACE_HEADER, traceId)

        try {
            filterChain.doFilter(request, response)
        } finally {
            MDC.remove(MDC_KEY)
        }
    }

    private fun normalize(value: String?) = value?.trim()

    private fun isValid(value: String?): Boolean {
        if(value.isNullOrEmpty()) return false
        if(value.any { it == '\n' || it == '\r' }) return false
        if(value.length > MAX_LEN) return false

        return true
    }

    private fun newTraceId(): String =
        UUID.randomUUID().toString()
}