package com.jammking.loopbox.adapter.out.persistence.jpa

import com.jammking.loopbox.domain.entity.image.ImageConfig
import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.entity.image.ImageVersion
import com.jammking.loopbox.domain.entity.image.ImageVersionId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import

@DataJpaTest
@Import(JpaImageVersionRepository::class)
class JpaImageVersionRepositoryTest {

    @Autowired
    private lateinit var repository: JpaImageVersionRepository

    @Test
    fun `save should store and return image version`() {
        val version = ImageVersion(
            id = ImageVersionId("version-1"),
            imageId = ImageId("image-1"),
            config = ImageConfig(description = "sky")
        )

        val saved = repository.save(version)
        val found = repository.findById(ImageVersionId("version-1"))

        assertEquals(saved.id.value, found?.id?.value)
        assertEquals("sky", found?.config?.description)
    }

    @Test
    fun `findByImageId should return versions and deleteByImageId should remove`() {
        repository.save(
            ImageVersion(
                id = ImageVersionId("version-1"),
                imageId = ImageId("image-1"),
                config = ImageConfig(description = "a")
            )
        )
        repository.save(
            ImageVersion(
                id = ImageVersionId("version-2"),
                imageId = ImageId("image-1"),
                config = ImageConfig(description = "b")
            )
        )

        val result = repository.findByImageId(ImageId("image-1")).map { it.id.value }.sorted()
        assertEquals(listOf("version-1", "version-2"), result)

        repository.deleteByImageId(ImageId("image-1"))
        assertNull(repository.findById(ImageVersionId("version-1")))
        assertNull(repository.findById(ImageVersionId("version-2")))
    }
}
