package com.jammking.loopbox.adapter.`in`.web.controller

import com.jammking.loopbox.adapter.`in`.web.dto.image.CreateImageResponse
import com.jammking.loopbox.adapter.`in`.web.dto.image.DeleteImageVersionResponse
import com.jammking.loopbox.adapter.`in`.web.dto.image.GenerateImageVersionRequest
import com.jammking.loopbox.adapter.`in`.web.dto.image.GenerateImageVersionResponse
import com.jammking.loopbox.adapter.`in`.web.dto.image.GetImageListResponse
import com.jammking.loopbox.adapter.`in`.web.dto.image.GetImageResponse
import com.jammking.loopbox.adapter.`in`.web.mapper.WebImageMapper.toWeb
import com.jammking.loopbox.adapter.`in`.web.support.AuthenticatedUserResolver
import com.jammking.loopbox.application.port.`in`.ImageManagementUseCase
import com.jammking.loopbox.application.port.`in`.ImageQueryUseCase
import com.jammking.loopbox.domain.entity.image.ImageConfig
import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.entity.image.ImageVersionId
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.task.ImageAiProvider
import com.jammking.loopbox.domain.exception.task.InvalidImageAiProvider
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/project/{projectId}/image")
class ImageController(
    private val imageQueryUseCase: ImageQueryUseCase,
    private val imageManagementUseCase: ImageManagementUseCase,
    private val authenticatedUserResolver: AuthenticatedUserResolver
) {

    @PostMapping("/create")
    fun createImage(
        @RequestHeader("Authorization", required = false) authorization: String?,
        @PathVariable projectId: String
    ): CreateImageResponse {
        val userId = authenticatedUserResolver.resolve(authorization)
        val image = imageManagementUseCase.createImage(userId, ProjectId(projectId))
        val webImage = image.toWeb()
        return CreateImageResponse(webImage)
    }

    @GetMapping("/{imageId}")
    fun getImage(
        @RequestHeader("Authorization", required = false) authorization: String?,
        @PathVariable projectId: String,
        @PathVariable imageId: String
    ): GetImageResponse {
        val userId = authenticatedUserResolver.resolve(authorization)
        val getResult = imageQueryUseCase.getImageDetail(userId, ImageId(imageId))
        return GetImageResponse.from(getResult)
    }

    @GetMapping
    fun getImageList(
        @RequestHeader("Authorization", required = false) authorization: String?,
        @PathVariable projectId: String
    ): GetImageListResponse {
        val userId = authenticatedUserResolver.resolve(authorization)
        val images = imageQueryUseCase.getImageListForProject(userId, ProjectId(projectId))
        return GetImageListResponse(
            images = images.map { it.toWeb() }
        )
    }

    @DeleteMapping("/{imageId}")
    fun deleteImage(
        @RequestHeader("Authorization", required = false) authorization: String?,
        @PathVariable projectId: String,
        @PathVariable imageId: String
    ) {
        val userId = authenticatedUserResolver.resolve(authorization)
        imageManagementUseCase.deleteImage(userId, ImageId(imageId))
    }

    @PostMapping("/{imageId}/version/generate")
    fun generateVersion(
        @RequestHeader("Authorization", required = false) authorization: String?,
        @PathVariable projectId: String,
        @PathVariable imageId: String,
        @RequestBody request: GenerateImageVersionRequest
    ): GenerateImageVersionResponse {
        val userId = authenticatedUserResolver.resolve(authorization)
        val provider = try {
            ImageAiProvider.valueOf(request.provider)
        } catch(e: IllegalArgumentException) {
            throw InvalidImageAiProvider(request.provider)
        }
        val command = ImageManagementUseCase.GenerateVersionCommand(
            userId = userId,
            imageId = ImageId(imageId),
            config = request.toImageConfig(),
            provider = provider
        )
        val image = imageManagementUseCase.generateVersion(command)
        val webImage = image.toWeb()
        return GenerateImageVersionResponse(webImage)
    }

    @DeleteMapping("/{imageId}/version/{versionId}")
    fun deleteVersion(
        @RequestHeader("Authorization", required = false) authorization: String?,
        @PathVariable projectId: String,
        @PathVariable imageId: String,
        @PathVariable versionId: String
    ): DeleteImageVersionResponse {
        val userId = authenticatedUserResolver.resolve(authorization)
        val image = imageManagementUseCase.deleteVersion(userId, ImageId(imageId), ImageVersionId(versionId))
        val webImage = image.toWeb()
        return DeleteImageVersionResponse(webImage)
    }

    private fun GenerateImageVersionRequest.toImageConfig() =
        ImageConfig(
            description = description,
            width = width,
            height = height
        )
}
