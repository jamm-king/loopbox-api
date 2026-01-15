package com.jammking.loopbox.application.port.`in`

import com.jammking.loopbox.domain.entity.project.Project
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.user.UserId

interface ProjectManagementUseCase {
    fun createProject(userId: UserId, title: String): Project
    fun deleteProject(userId: UserId, projectId: ProjectId)
    fun renameTitle(userId: UserId, projectId: ProjectId, newTitle: String)
}
