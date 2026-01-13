package com.jammking.loopbox.application.service

import com.jammking.loopbox.application.port.`in`.VideoManagementUseCase
import com.jammking.loopbox.domain.entity.video.Video
import com.jammking.loopbox.domain.entity.video.VideoImageGroup
import com.jammking.loopbox.domain.entity.video.VideoSegment
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.exception.image.ImageVersionNotFoundException
import com.jammking.loopbox.domain.exception.music.MusicVersionNotFoundException
import com.jammking.loopbox.domain.exception.project.ProjectNotFoundException
import com.jammking.loopbox.domain.exception.video.InvalidVideoEditException
import com.jammking.loopbox.domain.exception.video.VideoNotFoundException
import com.jammking.loopbox.application.port.out.VideoFileStorage
import com.jammking.loopbox.application.port.out.VideoRenderClient
import com.jammking.loopbox.domain.port.out.ImageVersionRepository
import com.jammking.loopbox.domain.port.out.MusicVersionRepository
import com.jammking.loopbox.domain.port.out.ProjectRepository
import com.jammking.loopbox.domain.port.out.VideoRepository
import com.jammking.loopbox.domain.port.out.AudioFileRepository
import com.jammking.loopbox.domain.port.out.ImageFileRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class VideoManagementService(
    private val projectRepository: ProjectRepository,
    private val videoRepository: VideoRepository,
    private val musicVersionRepository: MusicVersionRepository,
    private val imageVersionRepository: ImageVersionRepository,
    private val audioFileRepository: AudioFileRepository,
    private val imageFileRepository: ImageFileRepository,
    private val videoFileStorage: VideoFileStorage,
    private val videoRenderClient: VideoRenderClient
): VideoManagementUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun updateVideo(command: VideoManagementUseCase.UpdateVideoCommand): Video {
        val project = projectRepository.findById(command.projectId)
            ?: throw ProjectNotFoundException.byProjectId(command.projectId)

        val segments = command.segments.map { input ->
            val version = musicVersionRepository.findById(input.musicVersionId)
                ?: throw MusicVersionNotFoundException.byVersionId(input.musicVersionId)

            VideoSegment(
                musicVersionId = version.id,
                musicId = version.musicId,
                durationSeconds = version.durationSeconds
            )
        }

        val imageGroups = buildImageGroups(command, segments.size)

        val video = videoRepository.findByProjectId(project.id)
            ?: Video(projectId = project.id)

        video.updateTimeline(segments, imageGroups)
        val saved = videoRepository.save(video)
        log.info(
            "Updated video timeline: projectId={}, segments={}, imageGroups={}",
            project.id.value, segments.size, imageGroups.size
        )

        return saved
    }

    override fun requestRender(projectId: ProjectId): Video {
        val project = projectRepository.findById(projectId)
            ?: throw ProjectNotFoundException.byProjectId(projectId)
        log.info("Request render for projectId={}", project.id.value)

        val video = videoRepository.findByProjectId(projectId)
            ?: throw VideoNotFoundException.byProjectId(projectId)

        video.startRender()
        val saved = videoRepository.save(video)

        val renderCommand = buildRenderCommand(projectId, saved)
        log.info(
            "Queued video render: projectId={}, videoId={}, segments={}, imageGroups={}",
            projectId.value, saved.id.value, renderCommand.segments.size, renderCommand.imageGroups.size
        )

        return try {
            videoRenderClient.requestRender(renderCommand)
            saved
        } catch(e: Exception) {
            log.error("Failed to request video render: projectId={}, reason={}", projectId.value, e.message, e)
            saved.failRender()
            videoRepository.save(saved)
            throw e
        }
    }

    private fun buildRenderCommand(
        projectId: ProjectId,
        video: Video
    ): VideoRenderClient.RenderCommand {
        val segments = video.segments.map { segment ->
            val version = musicVersionRepository.findById(segment.musicVersionId)
                ?: throw MusicVersionNotFoundException.byVersionId(segment.musicVersionId)
            val fileId = version.fileId
                ?: throw InvalidVideoEditException("Invalid video edit: music version file is missing.")
            val file = audioFileRepository.findById(fileId)
                ?: throw InvalidVideoEditException("Invalid video edit: music file not found.")
            VideoRenderClient.RenderSegment(
                musicVersionId = segment.musicVersionId.value,
                audioPath = file.path,
                durationSeconds = segment.durationSeconds
            )
        }

        val imageGroups = video.imageGroups.map { group ->
            val version = imageVersionRepository.findById(group.imageVersionId)
                ?: throw ImageVersionNotFoundException.byVersionId(group.imageVersionId)
            val fileId = version.fileId
                ?: throw InvalidVideoEditException("Invalid video edit: image version file is missing.")
            val file = imageFileRepository.findById(fileId)
                ?: throw InvalidVideoEditException("Invalid video edit: image file not found.")
            VideoRenderClient.RenderImageGroup(
                imageVersionId = group.imageVersionId.value,
                imagePath = file.path,
                segmentIndexStart = group.segmentIndexStart,
                segmentIndexEnd = group.segmentIndexEnd
            )
        }

        val outputPath = videoFileStorage.prepareRenderPath(projectId, video.id)
        return VideoRenderClient.RenderCommand(
            projectId = projectId,
            videoId = video.id,
            outputPath = outputPath,
            segments = segments,
            imageGroups = imageGroups
        )
    }

    private fun buildImageGroups(
        command: VideoManagementUseCase.UpdateVideoCommand,
        segmentSize: Int
    ): List<VideoImageGroup> {
        if (command.imageGroups.isEmpty()) {
            return emptyList()
        }
        if (segmentSize == 0) {
            throw InvalidVideoEditException("Invalid video edit: image groups require at least one segment.")
        }

        val occupied = BooleanArray(segmentSize)
        return command.imageGroups.map { input ->
            if (input.segmentIndexStart < 0 || input.segmentIndexEnd < 0) {
                throw InvalidVideoEditException("Invalid video edit: segment index cannot be negative.")
            }
            if (input.segmentIndexStart > input.segmentIndexEnd) {
                throw InvalidVideoEditException("Invalid video edit: segmentIndexStart must be <= segmentIndexEnd.")
            }
            if (input.segmentIndexEnd >= segmentSize) {
                throw InvalidVideoEditException("Invalid video edit: segment index out of range.")
            }

            for (index in input.segmentIndexStart..input.segmentIndexEnd) {
                if (occupied[index]) {
                    throw InvalidVideoEditException("Invalid video edit: image groups cannot overlap.")
                }
                occupied[index] = true
            }

            val version = imageVersionRepository.findById(input.imageVersionId)
                ?: throw ImageVersionNotFoundException.byVersionId(input.imageVersionId)

            VideoImageGroup(
                imageVersionId = version.id,
                imageId = version.imageId,
                segmentIndexStart = input.segmentIndexStart,
                segmentIndexEnd = input.segmentIndexEnd
            )
        }
    }
}
