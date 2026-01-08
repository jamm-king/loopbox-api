package com.jammking.loopbox.adapter.`in`.web.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.file.Paths

@Configuration
class StaticResourceConfig(
    @Value("\${loopbox.storage.image-dir}")
    private val imageBaseDir: String,
    @Value("\${loopbox.storage.image-base-url}")
    private val imageBaseUrl: String
): WebMvcConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        val baseUrl = normalizeBaseUrl(imageBaseUrl)
        val basePath = Paths.get(imageBaseDir).toAbsolutePath().normalize().toUri().toString()
        registry.addResourceHandler("$baseUrl**")
            .addResourceLocations(basePath)
    }

    private fun normalizeBaseUrl(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return "/"
        val withSlash = if (trimmed.startsWith("/")) trimmed else "/$trimmed"
        return if (withSlash.endsWith("/")) withSlash else "$withSlash/"
    }
}
