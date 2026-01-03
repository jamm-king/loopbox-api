package com.jammking.loopbox.adapter.out.storage.file

import com.jammking.loopbox.application.exception.ResolveLocalAudioPortException
import com.jammking.loopbox.application.port.`in`.GetMusicVersionAudioUseCase
import com.jammking.loopbox.application.port.out.MusicFileStorage
import com.jammking.loopbox.application.port.out.ResolveLocalAudioPort
import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.music.MusicVersionId
import com.jammking.loopbox.domain.entity.project.ProjectId
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

@Component
class LocalMusicFileStorage(
    @Value("\${loopbox.storage.audio-dir}")
    private val audioBaseDir: String,
    private val okHttpClient: OkHttpClient = OkHttpClient()
): MusicFileStorage, ResolveLocalAudioPort {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun saveFromRemoteUrl(
        remoteUrl: String,
        projectId: ProjectId,
        musicId: MusicId,
        versionId: MusicVersionId
    ): String {

        val dir = Paths.get(audioBaseDir, projectId.value, musicId.value)
        try {
            Files.createDirectories(dir)
        } catch(e: Exception) {
            log.error("Failed To create directories for path={}", dir.toAbsolutePath(), e)
            return ""
        }

        val fileName = "${versionId.value}.mp3"
        val target = dir.resolve(fileName)

        return try {
            val request = Request.Builder()
                .url(URI(remoteUrl).toURL())
                .build()

            log.info("Downloading audio: url={}, target={}", remoteUrl, target.toAbsolutePath())

            okHttpClient.newCall(request).execute().use { response ->
                if(!response.isSuccessful) {
                    log.warn(
                        "Failed to download audio. url={}, code={}, message={}",
                        remoteUrl, response.code, response.message
                    )
                    return ""
                }

                val body = response.body
                if(body == null) {
                    log.warn("Empty body when downloading audio. url={}", remoteUrl)
                    return ""
                }

                body.byteStream().use { input ->
                    Files.newOutputStream(
                        target,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE
                    ).use { output ->
                        input.copyTo(output)
                    }
                }

                target.toString()
            }
        } catch(e: Exception) {
            log.error("Failed to download audio from url={}", remoteUrl, e)
            ""
        }
    }

    override fun resolve(pathStr: String): GetMusicVersionAudioUseCase.AudioStreamTarget {

        val path = safePath(pathStr)

        if(!Files.exists(path) || !Files.isRegularFile(path)) {
            log.error("Audio binary missing: path=$path")
            throw ResolveLocalAudioPortException.audioBinaryNotFound()
        }
        if(!Files.isReadable(path)) {
            log.error("Audio binary not readable: path=$path")
            throw ResolveLocalAudioPortException.audioBinaryNotFound()
        }

        val length = Files.size(path)
        val contentType = Files.probeContentType(path) ?: "application/octet-stream"

        return GetMusicVersionAudioUseCase.AudioStreamTarget(
            path = path,
            contentLength = length,
            contentType = contentType
        )

    }

    private fun safePath(raw: String): Path = Paths.get(raw).normalize()
}