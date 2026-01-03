package com.jammking.loopbox.application.port.`in`

import com.jammking.loopbox.domain.entity.project.Project
import com.jammking.loopbox.domain.entity.project.ProjectId

interface ProjectQueryUseCase {
    fun getProjectDetail(projectId: ProjectId): Project
    fun getAllProjects(): List<Project>
}