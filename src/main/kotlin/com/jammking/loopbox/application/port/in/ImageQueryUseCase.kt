package com.jammking.loopbox.application.port.`in`

import com.jammking.loopbox.domain.entity.image.Image
import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.entity.image.ImageVersion
import com.jammking.loopbox.domain.entity.project.ProjectId

interface ImageQueryUseCase {

    fun getImageDetail(imageId: ImageId): GetImageDetailResult
    fun getImageListForProject(projectId: ProjectId): List<Image>

    data class GetImageDetailResult(
        val image: Image,
        val versions: List<ImageVersion>
    )
}
