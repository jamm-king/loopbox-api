package com.jammking.loopbox.adapter.`in`.web.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.net.URI
import java.nio.file.Path
import java.nio.file.Paths

class StaticResourceConfigTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `toDirectoryUriString should keep resources under base dir`() {
        val baseUri = StaticResourceConfig.toDirectoryUriString(tempDir.toString())

        assertTrue(baseUri.endsWith("/"))
        assertTrue(baseUri.startsWith("file:"))

        val resolved = Paths.get(URI(baseUri).resolve("image.png")).toAbsolutePath().normalize()
        val expected = tempDir.resolve("image.png").toAbsolutePath().normalize()

        assertEquals(expected, resolved)
    }
}
