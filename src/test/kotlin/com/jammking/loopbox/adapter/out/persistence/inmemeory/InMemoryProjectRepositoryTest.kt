package com.jammking.loopbox.adapter.out.persistence.inmemeory

import com.jammking.loopbox.domain.entity.project.Project
import com.jammking.loopbox.domain.entity.project.ProjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class InMemoryProjectRepositoryTest {

    private val repository = InMemoryProjectRepository()

    @Test
    fun `save should store and return project`() {
        val project = Project(id = ProjectId("project-1"), title = "My Project")

        val saved = repository.save(project)
        val found = repository.findById(ProjectId("project-1"))

        assertEquals("project-1", saved.id.value)
        assertEquals("My Project", saved.title)
        assertEquals("project-1", found?.id?.value)
        assertEquals("My Project", found?.title)
    }

    @Test
    fun `findAll should return all projects`() {
        repository.save(Project(id = ProjectId("project-1"), title = "A"))
        repository.save(Project(id = ProjectId("project-2"), title = "B"))

        val result = repository.findAll().map { it.id.value }.sorted()

        assertEquals(listOf("project-1", "project-2"), result)
    }

    @Test
    fun `deleteById should remove project`() {
        repository.save(Project(id = ProjectId("project-1"), title = "A"))

        repository.deleteById(ProjectId("project-1"))

        assertNull(repository.findById(ProjectId("project-1")))
    }
}
