package com.jammking.loopbox.domain.port.out

import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.entity.image.ImageVersion
import com.jammking.loopbox.domain.entity.image.ImageVersionId

interface ImageVersionRepository {
    fun save(version: ImageVersion): ImageVersion
    fun findById(versionId: ImageVersionId): ImageVersion?
    fun findByImageId(imageId: ImageId): List<ImageVersion>
    fun deleteById(versionId: ImageVersionId)
    fun deleteByImageId(imageId: ImageId)
}
