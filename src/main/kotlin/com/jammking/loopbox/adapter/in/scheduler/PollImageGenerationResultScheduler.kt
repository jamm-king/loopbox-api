package com.jammking.loopbox.adapter.`in`.scheduler

import com.jammking.loopbox.application.port.`in`.PollImageGenerationResultUseCase
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class PollImageGenerationResultScheduler(
    private val pollImageGenerationResultUseCase: PollImageGenerationResultUseCase
) {

    @Scheduled(
        fixedDelayString = "\${loopbox.replicate.polling.fixed-delay-ms:60000}",
        initialDelayString = "\${loopbox.replicate.polling.initial-delay-ms:15000}"
    )
    fun poll() {
        pollImageGenerationResultUseCase.poll()
    }
}
