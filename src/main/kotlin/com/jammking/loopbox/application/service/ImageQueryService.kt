package com.jammking.loopbox.application.service

import com.jammking.loopbox.application.port.`in`.ImageQueryUseCase
import com.jammking.loopbox.application.port.`in`.ImageQueryUseCase.GetImageDetailResult
import com.jammking.loopbox.application.port.out.ResolveImageAccessPort
import com.jammking.loopbox.domain.entity.image.Image
import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.exception.image.ImageNotFoundException
import com.jammking.loopbox.domain.port.out.ImageFileRepository
import com.jammking.loopbox.domain.port.out.ImageRepository
import com.jammking.loopbox.domain.port.out.ImageVersionRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ImageQueryService(
    private val imageRepository: ImageRepository,
    private val versionRepository: ImageVersionRepository,
    private val fileRepository: ImageFileRepository,
    private val imageAccessResolver: ResolveImageAccessPort
): ImageQueryUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun getImageDetail(imageId: ImageId): GetImageDetailResult {
        val image = imageRepository.findById(imageId) ?: throw ImageNotFoundException.byImageId(imageId)
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

    override fun getImageListForProject(projectId: ProjectId): List<Image> =
        imageRepository.findByProjectId(projectId)
}
