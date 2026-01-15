package com.jammking.loopbox.application.port.`in`

import com.jammking.loopbox.domain.entity.image.Image
import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.entity.image.ImageVersion
import com.jammking.loopbox.domain.entity.image.ImageVersionId
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.user.UserId

interface ImageQueryUseCase {

    fun getImageDetail(userId: UserId, imageId: ImageId): GetImageDetailResult
    fun getImageListForProject(userId: UserId, projectId: ProjectId): List<Image>

    data class GetImageDetailResult(
        val image: Image,
        val versions: List<ImageVersion>,
        val versionUrls: Map<ImageVersionId, String> = emptyMap()
    )
}
