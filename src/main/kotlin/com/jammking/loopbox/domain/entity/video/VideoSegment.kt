package com.jammking.loopbox.domain.entity.video

import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.music.MusicVersionId
import java.util.UUID

data class VideoSegment(
    val id: VideoSegmentId = VideoSegmentId(UUID.randomUUID().toString()),
    val musicVersionId: MusicVersionId,
    val musicId: MusicId,
    val durationSeconds: Int
)
