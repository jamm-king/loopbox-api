package com.jammking.loopbox.adapter.out.storage.file

import com.jammking.loopbox.application.exception.PortErrorCode
import com.jammking.loopbox.application.exception.VideoFileStorageException
import com.jammking.loopbox.application.port.out.VideoFileStorage
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.video.VideoId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Paths

@Component
class LocalVideoFileStorage(
    @Value("\${loopbox.storage.video-dir}")
    private val videoBaseDir: String
): VideoFileStorage {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun prepareRenderPath(projectId: ProjectId, videoId: VideoId): String {
        val dir = Paths.get(videoBaseDir, projectId.value)
        try {
            Files.createDirectories(dir)
        } catch (e: Exception) {
            log.error("Failed To create directories for path={}", dir.toAbsolutePath(), e)
            throw VideoFileStorageException(
                code = PortErrorCode.TEMPORARY_UNAVAILABLE,
                message = "Failed to create video directory: ${dir.toAbsolutePath()}"
            )
        }

        val fileName = "${videoId.value}.mp4"
        return dir.resolve(fileName).toString()
    }
}
