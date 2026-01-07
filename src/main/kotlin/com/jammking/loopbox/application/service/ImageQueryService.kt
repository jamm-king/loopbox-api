package com.jammking.loopbox.application.service

import com.jammking.loopbox.application.port.`in`.ImageQueryUseCase
import com.jammking.loopbox.application.port.`in`.ImageQueryUseCase.GetImageDetailResult
import com.jammking.loopbox.domain.entity.image.Image
import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.exception.image.ImageNotFoundException
import com.jammking.loopbox.domain.port.out.ImageRepository
import com.jammking.loopbox.domain.port.out.ImageVersionRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ImageQueryService(
    private val imageRepository: ImageRepository,
    private val versionRepository: ImageVersionRepository
): ImageQueryUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun getImageDetail(imageId: ImageId): GetImageDetailResult {
        val image = imageRepository.findById(imageId) ?: throw ImageNotFoundException.byImageId(imageId)
        val versions = versionRepository.findByImageId(imageId)

        return GetImageDetailResult(image, versions)
    }

    override fun getImageListForProject(projectId: ProjectId): List<Image> =
        imageRepository.findByProjectId(projectId)
}
