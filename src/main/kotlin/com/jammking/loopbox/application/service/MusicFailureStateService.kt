package com.jammking.loopbox.application.service

import com.jammking.loopbox.domain.entity.music.Music
import com.jammking.loopbox.domain.port.out.MusicRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class MusicFailureStateService(
    private val musicRepository: MusicRepository
) {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun markVersionGenerationFailed(music: Music) {
        music.failVersionGeneration()
        musicRepository.save(music)
    }
}
