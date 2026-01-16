package com.jammking.loopbox.adapter.out.persistence.jpa

import com.jammking.loopbox.adapter.out.persistence.jpa.entity.ProjectJpaEntity
import com.jammking.loopbox.adapter.out.persistence.jpa.repository.ProjectJpaRepository
import com.jammking.loopbox.domain.entity.project.Project
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.port.out.ProjectRepository
import org.springframework.stereotype.Repository

@Repository
class JpaProjectRepository(
    private val repository: ProjectJpaRepository
) : ProjectRepository {
    override fun save(project: Project): Project {
        val saved = repository.save(ProjectJpaEntity.fromDomain(project))
        return saved.toDomain()
    }

    override fun findById(id: ProjectId): Project? {
        return repository.findById(id.value).orElse(null)?.toDomain()
    }

    override fun findAll(): List<Project> {
        return repository.findAll().map { it.toDomain() }
    }

    override fun deleteById(id: ProjectId) {
        repository.deleteById(id.value)
    }
}
