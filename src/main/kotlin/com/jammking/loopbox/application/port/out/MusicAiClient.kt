package com.jammking.loopbox.application.port.out

import com.jammking.loopbox.domain.entity.music.MusicConfig
import com.jammking.loopbox.domain.entity.task.ExternalId
import com.jammking.loopbox.domain.entity.task.MusicAiProvider

interface MusicAiClient {

    val provider: MusicAiProvider

    fun generate(command: GenerateCommand): GenerateResult
    fun expand(command: ExpandCommand): ExpandResult

    data class GenerateCommand(
        val title: String,
        val config: MusicConfig
    )

    data class GenerateResult(
        val externalId: ExternalId,
    )

    data class ExpandCommand(
        val config: MusicConfig
    )

    data class ExpandResult(
        val externalId: ExternalId,
    )
}