package com.jammking.loopbox.adapter.`in`.web.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class AsyncRequestConfig(
    @Value("\${loopbox.web.async-timeout-ms:0}")
    private val asyncTimeoutMs: Long
): WebMvcConfigurer {

    override fun configureAsyncSupport(configurer: AsyncSupportConfigurer) {
        configurer.setDefaultTimeout(asyncTimeoutMs)
    }
}
