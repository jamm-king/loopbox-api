package com.jammking.loopbox.adapter.out.persistence.jpa

import com.jammking.loopbox.domain.entity.image.Image
import com.jammking.loopbox.domain.entity.image.ImageConfig
import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.entity.project.ProjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("postgresql")
@Import(JpaImageRepository::class)
class JpaImageRepositoryTest {

    @Autowired
    private lateinit var repository: JpaImageRepository

    @Test
    fun `save should store and return image`() {
        val image = Image(
            id = ImageId("image-1"),
            projectId = ProjectId("project-1"),
            requestedConfig = ImageConfig(description = "sky")
        )

        val saved = repository.save(image)
        val found = repository.findById(ImageId("image-1"))

        assertEquals(saved.id.value, found?.id?.value)
        assertEquals("sky", found?.requestedConfig?.description)
    }

    @Test
    fun `findByProjectId should return images`() {
        repository.save(Image(id = ImageId("image-1"), projectId = ProjectId("project-1")))
        repository.save(Image(id = ImageId("image-2"), projectId = ProjectId("project-1")))
        repository.save(Image(id = ImageId("image-3"), projectId = ProjectId("project-2")))

        val result = repository.findByProjectId(ProjectId("project-1")).map { it.id.value }.sorted()

        assertEquals(listOf("image-1", "image-2"), result)
    }
}
