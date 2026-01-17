package com.jammking.loopbox.application.service

import com.jammking.loopbox.domain.entity.video.Video
import com.jammking.loopbox.domain.port.out.VideoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class VideoRenderFailureService(
    private val videoRepository: VideoRepository
) {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun markRenderFailed(video: Video) {
        video.failRender()
        videoRepository.save(video)
    }
}
