package com.jammking.loopbox.domain.port.out

import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.task.ExternalId
import com.jammking.loopbox.domain.entity.task.MusicAiProvider
import com.jammking.loopbox.domain.entity.task.MusicGenerationTask
import com.jammking.loopbox.domain.entity.task.MusicGenerationTaskId
import com.jammking.loopbox.domain.entity.task.MusicGenerationTaskStatus
import java.time.Instant

interface MusicGenerationTaskRepository {
    fun save(task: MusicGenerationTask): MusicGenerationTask
    fun findById(id: MusicGenerationTaskId): MusicGenerationTask?
    fun findByMusicId(musicId: MusicId): List<MusicGenerationTask>
    fun findByProviderAndExternalId(provider: MusicAiProvider, externalId: ExternalId): MusicGenerationTask?
    fun deleteByMusicId(musicId: MusicId)
    fun deleteByStatusBefore(status: MusicGenerationTaskStatus, before: Instant): Int
}
