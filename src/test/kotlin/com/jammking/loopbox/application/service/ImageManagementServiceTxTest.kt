package com.jammking.loopbox.application.service

import com.jammking.loopbox.adapter.out.persistence.jpa.JpaImageGenerationTaskRepository
import com.jammking.loopbox.adapter.out.persistence.jpa.JpaImageRepository
import com.jammking.loopbox.adapter.out.persistence.jpa.JpaImageVersionRepository
import com.jammking.loopbox.adapter.out.persistence.jpa.JpaProjectRepository
import com.jammking.loopbox.application.port.`in`.ImageManagementUseCase
import com.jammking.loopbox.application.port.out.ImageAiClient
import com.jammking.loopbox.application.port.out.ImageAiRouter
import com.jammking.loopbox.domain.entity.image.Image
import com.jammking.loopbox.domain.entity.image.ImageConfig
import com.jammking.loopbox.domain.entity.image.ImageOperation
import com.jammking.loopbox.domain.entity.image.ImageStatus
import com.jammking.loopbox.domain.entity.project.Project
import com.jammking.loopbox.domain.entity.task.ImageAiProvider
import com.jammking.loopbox.domain.entity.user.UserId
import com.jammking.loopbox.domain.port.out.ImageRepository
import com.jammking.loopbox.domain.port.out.ProjectRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.beans.factory.annotation.Autowired
import jakarta.persistence.EntityManager

@DataJpaTest
@ActiveProfiles("postgresql")
@Import(
    ImageManagementService::class,
    ImageFailureStateService::class,
    JpaProjectRepository::class,
    JpaImageRepository::class,
    JpaImageVersionRepository::class,
    JpaImageGenerationTaskRepository::class,
    ImageManagementServiceTxTest.ImageAiTestConfig::class
)
class ImageManagementServiceTxTest(
) {
    @Autowired
    private lateinit var imageManagementService: ImageManagementService

    @Autowired
    private lateinit var projectRepository: ProjectRepository

    @Autowired
    private lateinit var imageRepository: ImageRepository

    @Autowired
    private lateinit var entityManager: EntityManager

    @Test
    fun `generateVersion persists failure state even when ai throws`() {
        val ownerId = UserId("user-1")
        val project = projectRepository.save(Project(ownerUserId = ownerId, title = "Project"))
        val image = imageRepository.save(Image(projectId = project.id))

        assertThrows(IllegalStateException::class.java) {
            imageManagementService.generateVersion(
                ImageManagementUseCase.GenerateVersionCommand(
                    userId = ownerId,
                    imageId = image.id,
                    config = ImageConfig(),
                    provider = ImageAiProvider.REPLICATE_GOOGLE_IMAGEN_4
                )
            )
        }

        entityManager.clear()
        val persisted = imageRepository.findById(image.id)
        assertEquals(ImageStatus.FAILED, persisted?.status)
        assertEquals(ImageOperation.GENERATE_VERSION, persisted?.lastOperation)
    }

    @TestConfiguration
    class ImageAiTestConfig {
        @Bean
        fun imageAiRouter(): ImageAiRouter = object : ImageAiRouter {
            override fun getClient(provider: ImageAiProvider): ImageAiClient = object : ImageAiClient {
                override val provider: ImageAiProvider = provider

                override fun generate(command: ImageAiClient.GenerateCommand): ImageAiClient.GenerateResult {
                    throw IllegalStateException("boom")
                }

                override fun fetchResult(command: ImageAiClient.FetchResultCommand): ImageAiClient.FetchResult {
                    throw IllegalStateException("boom")
                }
            }
        }
    }
}
