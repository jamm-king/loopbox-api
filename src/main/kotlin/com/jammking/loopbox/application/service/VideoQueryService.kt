package com.jammking.loopbox.application.service

import com.jammking.loopbox.application.port.`in`.VideoQueryUseCase
import com.jammking.loopbox.domain.entity.project.Project
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.user.UserId
import com.jammking.loopbox.domain.entity.video.Video
import com.jammking.loopbox.domain.exception.project.InvalidProjectOwnerException
import com.jammking.loopbox.domain.exception.project.ProjectNotFoundException
import com.jammking.loopbox.domain.port.out.ProjectRepository
import com.jammking.loopbox.domain.port.out.VideoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class VideoQueryService(
    private val projectRepository: ProjectRepository,
    private val videoRepository: VideoRepository
): VideoQueryUseCase {

    @Transactional
    override fun getVideoDetail(userId: UserId, projectId: ProjectId): Video {
        val project = projectRepository.findById(projectId)
            ?: throw ProjectNotFoundException.byProjectId(projectId)
        requireOwner(project, userId)

        val existing = videoRepository.findByProjectId(project.id)
        if (existing != null) {
            return existing
        }

        val video = Video(projectId = project.id)
        return videoRepository.save(video)
    }

    private fun requireOwner(project: Project, userId: UserId) {
        if (project.ownerUserId != userId) {
            throw InvalidProjectOwnerException(project.id, userId, project.ownerUserId)
        }
    }
}
