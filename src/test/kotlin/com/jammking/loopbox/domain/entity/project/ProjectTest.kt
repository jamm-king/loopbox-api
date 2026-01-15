package com.jammking.loopbox.domain.entity.project

import com.jammking.loopbox.domain.exception.project.InvalidProjectStateException
import com.jammking.loopbox.domain.exception.project.InvalidProjectTitleException
import com.jammking.loopbox.domain.entity.user.UserId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Instant

class ProjectTest {

    @Test
    fun `constructor should throw exception when title is blank`() {
        assertThrows(InvalidProjectTitleException::class.java) {
            Project(ownerUserId = UserId("user-1"), title = "")
        }
    }

    @Test
    fun `rename should update title and updatedAt`() {
        // Given
        val project = Project(ownerUserId = UserId("user-1"), title = "Old Title")
        val now = Instant.now().plusSeconds(10)

        // When
        project.rename("New Title", now)

        // Then
        assertEquals("New Title", project.title)
        assertEquals(now, project.updatedAt)
    }

    @Test
    fun `rename should throw exception when title is blank`() {
        // Given
        val project = Project(ownerUserId = UserId("user-1"), title = "Old Title")

        // When & Then
        assertThrows(InvalidProjectTitleException::class.java) {
            project.rename("")
        }
    }

    @Test
    fun `markDraft should update status to DRAFT`() {
        // Given
        val project = Project(ownerUserId = UserId("user-1"), title = "Title", status = ProjectStatus.MUSIC_READY)
        val now = Instant.now().plusSeconds(10)

        // When
        project.markDraft(now)

        // Then
        assertEquals(ProjectStatus.DRAFT, project.status)
        assertEquals(now, project.updatedAt)
    }

    @Test
    fun `markDraft should throw exception if not in MUSIC_READY state`() {
        // Given
        val project = Project(ownerUserId = UserId("user-1"), title = "Title", status = ProjectStatus.DRAFT)

        // When & Then
        assertThrows(InvalidProjectStateException::class.java) {
            project.markDraft()
        }
    }

    @Test
    fun `markMusicReady should update status to MUSIC_READY`() {
        // Given
        val project = Project(ownerUserId = UserId("user-1"), title = "Title", status = ProjectStatus.DRAFT)
        val now = Instant.now().plusSeconds(10)

        // When
        project.markMusicReady(now)

        // Then
        assertEquals(ProjectStatus.MUSIC_READY, project.status)
        assertEquals(now, project.updatedAt)
    }
}
