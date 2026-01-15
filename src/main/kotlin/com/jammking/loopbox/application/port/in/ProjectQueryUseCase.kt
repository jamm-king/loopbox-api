package com.jammking.loopbox.application.port.`in`

import com.jammking.loopbox.domain.entity.project.Project
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.user.UserId

interface ProjectQueryUseCase {
    fun getProjectDetail(userId: UserId, projectId: ProjectId): Project
    fun getAllProjects(userId: UserId): List<Project>
}
