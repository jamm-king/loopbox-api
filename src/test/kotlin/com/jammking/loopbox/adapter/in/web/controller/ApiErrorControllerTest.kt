package com.jammking.loopbox.adapter.`in`.web.controller

import com.jammking.loopbox.adapter.`in`.web.dto.error.ErrorResponse
import com.jammking.loopbox.adapter.`in`.web.dto.error.ErrorResponseFactory
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.web.servlet.error.ErrorAttributes
import org.springframework.http.HttpStatus
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(ApiErrorController::class)
class ApiErrorControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var errorResponseFactory: ErrorResponseFactory

    @MockitoBean
    private lateinit var errorAttributes: ErrorAttributes

    @Test
    fun `handleError should return error response`() {
        // Given
        val response = ErrorResponse(
            code = "ENDPOINT_NOT_FOUND",
            category = "SYSTEM",
            message = "No such endpoint.",
            path = "/missing",
            traceId = "trace-1",
            details = null
        )
        whenever(errorResponseFactory.fromErrorAttributes(any()))
            .thenReturn(HttpStatus.NOT_FOUND to response)

        // When & Then
        mockMvc.perform(get("/error"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("ENDPOINT_NOT_FOUND"))
            .andExpect(jsonPath("$.category").value("SYSTEM"))
            .andExpect(jsonPath("$.message").value("No such endpoint."))
    }
}
