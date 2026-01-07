package com.jammking.loopbox.adapter.out.storage.file

import com.jammking.loopbox.application.exception.MusicFileStorageException
import com.jammking.loopbox.application.exception.ResolveLocalAudioPortException
import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.music.MusicVersionId
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.net.InetSocketAddress
import java.nio.file.Files
import java.nio.file.Path

class LocalMusicFileStorageTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `saveFromRemoteUrl should download and save file`() {
        val bytes = "audio-bytes".toByteArray()
        val server = HttpServer.create(InetSocketAddress("localhost", 0), 0)
        server.createContext("/audio") { exchange ->
            exchange.sendResponseHeaders(200, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
        server.start()

        try {
            val storage = LocalMusicFileStorage(tempDir.toString())
            val result = storage.saveFromRemoteUrl(
                remoteUrl = "http://localhost:${server.address.port}/audio",
                projectId = ProjectId("project-1"),
                musicId = MusicId("music-1"),
                versionId = MusicVersionId("version-1")
            )

            val expectedPath = tempDir.resolve("project-1").resolve("music-1").resolve("version-1.mp3")
            assertEquals(expectedPath.toString(), result)
            assertArrayEquals(bytes, Files.readAllBytes(expectedPath))
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun `saveFromRemoteUrl should throw when response is not successful`() {
        val server = HttpServer.create(InetSocketAddress("localhost", 0), 0)
        server.createContext("/audio") { exchange ->
            exchange.sendResponseHeaders(500, -1)
            exchange.close()
        }
        server.start()

        try {
            val storage = LocalMusicFileStorage(tempDir.toString())
            assertThrows(MusicFileStorageException::class.java) {
                storage.saveFromRemoteUrl(
                    remoteUrl = "http://localhost:${server.address.port}/audio",
                    projectId = ProjectId("project-1"),
                    musicId = MusicId("music-1"),
                    versionId = MusicVersionId("version-1")
                )
            }

            val expectedPath = tempDir.resolve("project-1").resolve("music-1").resolve("version-1.mp3")
            assertEquals(false, Files.exists(expectedPath))
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun `resolve should return audio stream target for existing file`() {
        val filePath = tempDir.resolve("audio.loopbox")
        Files.write(filePath, "data".toByteArray())
        val expectedContentType = Files.probeContentType(filePath) ?: "application/octet-stream"

        val storage = LocalMusicFileStorage(tempDir.toString())
        val result = storage.resolve(filePath.toString())

        assertEquals(filePath, result.path)
        assertEquals(4L, result.contentLength)
        assertEquals(expectedContentType, result.contentType)
    }

    @Test
    fun `resolve should throw when file missing`() {
        val storage = LocalMusicFileStorage(tempDir.toString())

        assertThrows(ResolveLocalAudioPortException::class.java) {
            storage.resolve(tempDir.resolve("missing.mp3").toString())
        }
    }
}
