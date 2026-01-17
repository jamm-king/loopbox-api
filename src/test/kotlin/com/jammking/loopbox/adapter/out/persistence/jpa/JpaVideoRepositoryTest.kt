package com.jammking.loopbox.adapter.out.persistence.jpa

import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.entity.image.ImageVersionId
import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.music.MusicVersionId
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.video.Video
import com.jammking.loopbox.domain.entity.video.VideoImageGroup
import com.jammking.loopbox.domain.entity.video.VideoSegment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("postgresql")
@Import(JpaVideoRepository::class)
class JpaVideoRepositoryTest {

    @Autowired
    private lateinit var repository: JpaVideoRepository

    @Test
    fun `save should store and return video with segments`() {
        val segments = listOf(
            VideoSegment(
                musicVersionId = MusicVersionId("music-version-1"),
                musicId = MusicId("music-1"),
                durationSeconds = 10
            )
        )
        val imageGroups = listOf(
            VideoImageGroup(
                imageVersionId = ImageVersionId("image-version-1"),
                imageId = ImageId("image-1"),
                segmentIndexStart = 0,
                segmentIndexEnd = 0
            )
        )
        val video = Video(
            projectId = ProjectId("project-1"),
            segments = segments,
            imageGroups = imageGroups
        )

        val saved = repository.save(video)
        val found = repository.findByProjectId(ProjectId("project-1"))

        assertEquals(saved.id.value, found?.id?.value)
        assertEquals(1, found?.segments?.size)
        assertEquals("music-version-1", found?.segments?.first()?.musicVersionId?.value)
        assertEquals(1, found?.imageGroups?.size)
        assertEquals("image-version-1", found?.imageGroups?.first()?.imageVersionId?.value)
    }
}
