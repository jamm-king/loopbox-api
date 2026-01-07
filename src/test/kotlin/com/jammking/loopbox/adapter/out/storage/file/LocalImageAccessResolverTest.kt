package com.jammking.loopbox.adapter.out.storage.file

import com.jammking.loopbox.application.exception.ResolveImageAccessPortException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class LocalImageAccessResolverTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `resolve should return base url with relative path`() {
        val imageDir = tempDir.resolve("images")
        Files.createDirectories(imageDir)
        val file = imageDir.resolve("project-1/image-1/version-1.png")
        Files.createDirectories(file.parent)
        Files.writeString(file, "data")

        val resolver = LocalImageAccessResolver(
            imageBaseDir = imageDir.toString(),
            imageBaseUrl = "https://cdn.example.com/static/image/"
        )

        val result = resolver.resolve(file.toString())

        assertEquals(
            "https://cdn.example.com/static/image/project-1/image-1/version-1.png",
            result.url
        )
    }

    @Test
    fun `resolve should reject path outside base dir`() {
        val imageDir = tempDir.resolve("images")
        Files.createDirectories(imageDir)
        val outside = tempDir.resolve("outside.png")
        Files.writeString(outside, "data")

        val resolver = LocalImageAccessResolver(
            imageBaseDir = imageDir.toString(),
            imageBaseUrl = "https://cdn.example.com/static/image"
        )

        assertThrows(ResolveImageAccessPortException::class.java) {
            resolver.resolve(outside.toString())
        }
    }

    @Test
    fun `resolve should reject missing file`() {
        val imageDir = tempDir.resolve("images")
        Files.createDirectories(imageDir)
        val missing = imageDir.resolve("missing.png")

        val resolver = LocalImageAccessResolver(
            imageBaseDir = imageDir.toString(),
            imageBaseUrl = "https://cdn.example.com/static/image"
        )

        assertThrows(ResolveImageAccessPortException::class.java) {
            resolver.resolve(missing.toString())
        }
    }
}
