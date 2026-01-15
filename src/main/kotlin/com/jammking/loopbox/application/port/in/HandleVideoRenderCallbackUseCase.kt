package com.jammking.loopbox.application.port.`in`

import com.jammking.loopbox.domain.entity.project.ProjectId

interface HandleVideoRenderCallbackUseCase {
    fun handle(command: Command)

    data class Command(
        val projectId: ProjectId,
        val status: Status,
        val outputPath: String? = null,
        val message: String? = null
    )

    enum class Status {
        RENDERING,
        COMPLETED,
        FAILED
    }
}
