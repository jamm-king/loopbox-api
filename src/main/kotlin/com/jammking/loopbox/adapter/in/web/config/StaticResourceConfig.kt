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
        val basePath = toDirectoryUriString(imageBaseDir)
        registry.addResourceHandler("$baseUrl**")
            .addResourceLocations(basePath)
    }

    private fun normalizeBaseUrl(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return "/"
        val withSlash = if (trimmed.startsWith("/")) trimmed else "/$trimmed"
        return if (withSlash.endsWith("/")) withSlash else "$withSlash/"
    }

    companion object {
        internal fun toDirectoryUriString(dir: String): String {
            val basePath = Paths.get(dir).toAbsolutePath().normalize().toUri().toString()
            return if (basePath.endsWith("/")) basePath else "$basePath/"
        }
    }
}
