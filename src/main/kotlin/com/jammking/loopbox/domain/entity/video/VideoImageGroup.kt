package com.jammking.loopbox.domain.entity.video

import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.entity.image.ImageVersionId
import java.util.UUID

data class VideoImageGroup(
    val id: VideoImageGroupId = VideoImageGroupId(UUID.randomUUID().toString()),
    val imageVersionId: ImageVersionId,
    val imageId: ImageId,
    val segmentIndexStart: Int,
    val segmentIndexEnd: Int
)
