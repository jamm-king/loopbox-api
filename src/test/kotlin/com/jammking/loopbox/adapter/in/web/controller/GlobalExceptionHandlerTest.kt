package com.jammking.loopbox.adapter.`in`.web.controller

import com.jammking.loopbox.adapter.`in`.web.dto.error.ErrorResponse
import com.jammking.loopbox.adapter.`in`.web.dto.error.ErrorResponseFactory
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.exception.project.InvalidProjectTitleException
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@WebMvcTest(GlobalExceptionHandlerTestController::class)
@Import(GlobalExceptionHandler::class)
class GlobalExceptionHandlerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var errorResponseFactory: ErrorResponseFactory

    @Test
    fun `handleLoopboxException should return mapped error response`() {
        // Given
        val response = ErrorResponse(
            code = "INVALID_PROJECT_TITLE",
            category = "VALIDATION",
            message = "Invalid project title.",
            path = "/test/invalid",
            traceId = "trace-1",
            details = null
        )
        whenever(errorResponseFactory.fromLoopbox(any(), any())).thenReturn(response)

        // When & Then
        mockMvc.perform(get("/test/invalid"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("INVALID_PROJECT_TITLE"))
            .andExpect(jsonPath("$.category").value("VALIDATION"))
            .andExpect(jsonPath("$.message").value("Invalid project title."))
    }

}

@RestController
class GlobalExceptionHandlerTestController {
    @GetMapping("/test/invalid")
    fun invalid(): String {
        throw InvalidProjectTitleException(ProjectId("project-1"), "bad")
    }
}
