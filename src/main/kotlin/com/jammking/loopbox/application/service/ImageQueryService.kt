package com.jammking.loopbox.application.service

import com.jammking.loopbox.application.port.`in`.ImageQueryUseCase
import com.jammking.loopbox.application.port.`in`.ImageQueryUseCase.GetImageDetailResult
import com.jammking.loopbox.application.port.out.ResolveImageAccessPort
import com.jammking.loopbox.domain.entity.image.Image
import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.entity.project.Project
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.user.UserId
import com.jammking.loopbox.domain.exception.image.ImageNotFoundException
import com.jammking.loopbox.domain.exception.project.InvalidProjectOwnerException
import com.jammking.loopbox.domain.exception.project.ProjectNotFoundException
import com.jammking.loopbox.domain.port.out.ImageFileRepository
import com.jammking.loopbox.domain.port.out.ImageRepository
import com.jammking.loopbox.domain.port.out.ImageVersionRepository
import com.jammking.loopbox.domain.port.out.ProjectRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ImageQueryService(
    private val imageRepository: ImageRepository,
    private val versionRepository: ImageVersionRepository,
    private val fileRepository: ImageFileRepository,
    private val imageAccessResolver: ResolveImageAccessPort,
    private val projectRepository: ProjectRepository
): ImageQueryUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun getImageDetail(userId: UserId, imageId: ImageId): GetImageDetailResult {
        val image = imageRepository.findById(imageId) ?: throw ImageNotFoundException.byImageId(imageId)
        val project = projectRepository.findById(image.projectId)
            ?: throw ProjectNotFoundException.byProjectId(image.projectId)
        requireOwner(project, userId)
        val versions = versionRepository.findByImageId(imageId)
        val versionUrls = versions.mapNotNull { version ->
            val fileId = version.fileId ?: return@mapNotNull null
            val file = fileRepository.findById(fileId) ?: return@mapNotNull null
            try {
                val target = imageAccessResolver.resolve(file.path)
                version.id to target.url
            } catch(e: Exception) {
                log.warn(
                    "Failed to resolve image access url: versionId={}, fileId={}, reason={}",
                    version.id.value, fileId.value, e.message
                )
                null
            }
        }.toMap()

        return GetImageDetailResult(image, versions, versionUrls)
    }

    override fun getImageListForProject(userId: UserId, projectId: ProjectId): List<Image> {
        val project = projectRepository.findById(projectId)
            ?: throw ProjectNotFoundException.byProjectId(projectId)
        requireOwner(project, userId)
        return imageRepository.findByProjectId(projectId)
    }

    private fun requireOwner(project: Project, userId: UserId) {
        if (project.ownerUserId != userId) {
            throw InvalidProjectOwnerException(project.id, userId, project.ownerUserId)
        }
    }
}
