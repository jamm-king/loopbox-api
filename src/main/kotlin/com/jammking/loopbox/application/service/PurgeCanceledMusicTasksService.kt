package com.jammking.loopbox.application.service

import com.jammking.loopbox.application.port.`in`.PurgeCanceledMusicTasksUseCase
import com.jammking.loopbox.domain.entity.task.MusicGenerationTaskStatus
import com.jammking.loopbox.domain.port.out.MusicGenerationTaskRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant

@Service
@Transactional
class PurgeCanceledMusicTasksService(
    private val taskRepository: MusicGenerationTaskRepository
): PurgeCanceledMusicTasksUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun purge(): Int {
        val cutoff = Instant.now().minus(Duration.ofDays(7))
        val deleted = taskRepository.deleteByStatusBefore(MusicGenerationTaskStatus.CANCELED, cutoff)
        if (deleted > 0) {
            log.info("Purged canceled music generation tasks: count={}, cutoff={}", deleted, cutoff)
        }
        return deleted
    }
}
