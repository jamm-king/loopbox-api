package com.jammking.loopbox.adapter.`in`.scheduler

import com.jammking.loopbox.application.port.`in`.PurgeCanceledMusicTasksUseCase
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class PurgeCanceledMusicTasksScheduler(
    private val purgeCanceledMusicTasksUseCase: PurgeCanceledMusicTasksUseCase
) {

    @Scheduled(cron = "0 0 3 * * *")
    fun purgeCanceledTasks() {
        purgeCanceledMusicTasksUseCase.purge()
    }
}
