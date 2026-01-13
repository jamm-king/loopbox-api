package com.jammking.loopbox.adapter.`in`.web.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer

class AsyncRequestConfigTest {

    @Test
    fun `configureAsyncSupport should set default timeout`() {
        val config = AsyncRequestConfig(asyncTimeoutMs = 600000)
        val configurer = AsyncSupportConfigurer()

        config.configureAsyncSupport(configurer)

        val field = AsyncSupportConfigurer::class.java.declaredFields
            .firstOrNull { it.name.contains("timeout", ignoreCase = true) }
            ?: throw IllegalStateException("Timeout field not found on AsyncSupportConfigurer")
        field.isAccessible = true
        val timeout = field.get(configurer) as Long?
        assertEquals(600000, timeout)
    }
}
