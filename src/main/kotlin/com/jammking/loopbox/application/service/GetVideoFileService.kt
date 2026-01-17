package com.jammking.loopbox.application.service

import com.jammking.loopbox.application.port.`in`.GetVideoFileUseCase
import com.jammking.loopbox.application.port.out.ResolveLocalVideoPort
import com.jammking.loopbox.domain.entity.project.Project
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.user.UserId
import com.jammking.loopbox.domain.exception.file.VideoFileNotFoundException
import com.jammking.loopbox.domain.exception.project.InvalidProjectOwnerException
import com.jammking.loopbox.domain.exception.project.ProjectNotFoundException
import com.jammking.loopbox.domain.exception.video.InvalidVideoStateException
import com.jammking.loopbox.domain.exception.video.VideoNotFoundException
import com.jammking.loopbox.domain.port.out.ProjectRepository
import com.jammking.loopbox.domain.port.out.VideoFileRepository
import com.jammking.loopbox.domain.port.out.VideoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class GetVideoFileService(
    private val projectRepository: ProjectRepository,
    private val videoRepository: VideoRepository,
    private val videoFileRepository: VideoFileRepository,
    private val resolveLocalVideoPort: ResolveLocalVideoPort
): GetVideoFileUseCase {

    override fun getVideoTarget(userId: UserId, projectId: ProjectId): GetVideoFileUseCase.VideoStreamTarget {
        val project = projectRepository.findById(projectId)
            ?: throw ProjectNotFoundException.byProjectId(projectId)
        requireOwner(project, userId)

        val video = videoRepository.findByProjectId(project.id)
            ?: throw VideoNotFoundException.byProjectId(project.id)

        if (video.status != com.jammking.loopbox.domain.entity.video.VideoStatus.READY) {
            throw InvalidVideoStateException(video, "get file")
        }

        val fileId = video.fileId
            ?: throw InvalidVideoStateException(video, "get file")

        val file = videoFileRepository.findById(fileId)
            ?: throw VideoFileNotFoundException.byVideoFileId(fileId)

        return resolveLocalVideoPort.resolve(file.path)
    }

    private fun requireOwner(project: Project, userId: UserId) {
        if (project.ownerUserId != userId) {
            throw InvalidProjectOwnerException(project.id, userId, project.ownerUserId)
        }
    }
}
