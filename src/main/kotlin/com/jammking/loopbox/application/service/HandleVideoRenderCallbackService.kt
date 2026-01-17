package com.jammking.loopbox.application.service

import com.jammking.loopbox.application.port.`in`.HandleVideoRenderCallbackUseCase
import com.jammking.loopbox.domain.entity.file.VideoFile
import com.jammking.loopbox.domain.exception.project.ProjectNotFoundException
import com.jammking.loopbox.domain.exception.video.VideoNotFoundException
import com.jammking.loopbox.domain.port.out.ProjectRepository
import com.jammking.loopbox.domain.port.out.VideoFileRepository
import com.jammking.loopbox.domain.port.out.VideoRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class HandleVideoRenderCallbackService(
    private val projectRepository: ProjectRepository,
    private val videoRepository: VideoRepository,
    private val videoFileRepository: VideoFileRepository
): HandleVideoRenderCallbackUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun handle(command: HandleVideoRenderCallbackUseCase.Command) {
        val project = projectRepository.findById(command.projectId)
            ?: throw ProjectNotFoundException.byProjectId(command.projectId)

        val video = videoRepository.findByProjectId(project.id)
            ?: throw VideoNotFoundException.byProjectId(project.id)

        when (command.status) {
            HandleVideoRenderCallbackUseCase.Status.RENDERING -> {
                log.info("Video render in progress: projectId={}, message={}", project.id.value, command.message)
            }
            HandleVideoRenderCallbackUseCase.Status.COMPLETED -> {
                val outputPath = command.outputPath
                    ?: throw IllegalArgumentException("Video render completed without outputPath.")
                val savedFile = videoFileRepository.save(VideoFile(path = outputPath))
                video.completeRender(savedFile.id)
                val savedVideo = videoRepository.save(video)
                log.info(
                    "Video render completed: projectId={}, videoId={}, fileId={}",
                    project.id.value, savedVideo.id.value, savedFile.id.value
                )
            }
            HandleVideoRenderCallbackUseCase.Status.FAILED -> {
                video.failRender()
                val savedVideo = videoRepository.save(video)
                log.warn(
                    "Video render failed: projectId={}, videoId={}, message={}",
                    project.id.value, savedVideo.id.value, command.message
                )
            }
        }
    }
}
