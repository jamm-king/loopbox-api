package com.jammking.loopbox.application.service

import com.jammking.loopbox.domain.port.out.RefreshTokenRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class RefreshTokenCleanupService(
    private val refreshTokenRepository: RefreshTokenRepository
) {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun deleteByToken(token: String) {
        refreshTokenRepository.deleteByToken(token)
    }
}
