package com.jammking.loopbox.domain.port.out

import com.jammking.loopbox.domain.entity.project.Project
import com.jammking.loopbox.domain.entity.project.ProjectId

interface ProjectRepository {
    fun save(project: Project): Project
    fun findById(id: ProjectId): Project?
    fun findAll(): List<Project>
    fun deleteById(id: ProjectId)
}