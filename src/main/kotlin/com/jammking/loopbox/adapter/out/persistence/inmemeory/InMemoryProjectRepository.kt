package com.jammking.loopbox.adapter.out.persistence.inmemeory

import com.jammking.loopbox.domain.port.out.ProjectRepository
import com.jammking.loopbox.domain.entity.project.Project
import com.jammking.loopbox.domain.entity.project.ProjectId
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

@Repository
@Profile("inmemory")
class InMemoryProjectRepository: ProjectRepository {

    private val store = ConcurrentHashMap<String, Project>()

    override fun save(project: Project): Project {
        val stored = project.copy()
        store[project.id.value] = stored
        return stored.copy()
    }

    override fun findById(id: ProjectId): Project? =
        store[id.value]

    override fun findAll(): List<Project> =
        store.values.toList()

    override fun deleteById(id: ProjectId) {
        store.remove(id.value)
    }
}
