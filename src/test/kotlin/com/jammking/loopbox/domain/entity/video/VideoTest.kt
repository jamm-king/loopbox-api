package com.jammking.loopbox.domain.entity.video

import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.music.MusicVersionId
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.file.VideoFileId
import com.jammking.loopbox.domain.exception.video.InvalidVideoStateException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Instant

class VideoTest {

    private val projectId = ProjectId("project-1")

    @Test
    fun `updateTimeline should update segments and status`() {
        // Given
        val video = Video(projectId = projectId)
        val segment = VideoSegment(
            musicVersionId = MusicVersionId("music-version-1"),
            musicId = MusicId("music-1"),
            durationSeconds = 30
        )
        val now = Instant.now().plusSeconds(10)

        // When
        video.updateTimeline(listOf(segment), emptyList(), now)

        // Then
        assertEquals(1, video.segments.size)
        assertEquals(VideoStatus.DRAFT, video.status)
        assertEquals(now, video.updatedAt)
    }

    @Test
    fun `totalDurationSeconds should sum segments`() {
        // Given
        val video = Video(
            projectId = projectId,
            segments = listOf(
                VideoSegment(
                    musicVersionId = MusicVersionId("music-version-1"),
                    musicId = MusicId("music-1"),
                    durationSeconds = 30
                ),
                VideoSegment(
                    musicVersionId = MusicVersionId("music-version-2"),
                    musicId = MusicId("music-2"),
                    durationSeconds = 45
                )
            )
        )

        // Then
        assertEquals(75, video.totalDurationSeconds())
    }

    @Test
    fun `completeRender should throw if not rendering`() {
        // Given
        val video = Video(projectId = projectId)

        // When & Then
        assertThrows(InvalidVideoStateException::class.java) {
            video.completeRender(VideoFileId("file-1"))
        }
    }
}
