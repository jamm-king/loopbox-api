package com.jammking.loopbox.application.port.`in`

import com.jammking.loopbox.domain.entity.project.Project
import com.jammking.loopbox.domain.entity.project.ProjectId

interface ProjectManagementUseCase {
    fun createProject(title: String): Project
    fun deleteProject(projectId: ProjectId)
    fun renameTitle(projectId: ProjectId, newTitle: String)
}