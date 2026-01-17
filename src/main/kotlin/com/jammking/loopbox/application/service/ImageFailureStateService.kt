package com.jammking.loopbox.application.service

import com.jammking.loopbox.domain.entity.image.Image
import com.jammking.loopbox.domain.port.out.ImageRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class ImageFailureStateService(
    private val imageRepository: ImageRepository
) {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun markVersionGenerationFailed(image: Image) {
        image.failVersionGeneration()
        imageRepository.save(image)
    }
}
