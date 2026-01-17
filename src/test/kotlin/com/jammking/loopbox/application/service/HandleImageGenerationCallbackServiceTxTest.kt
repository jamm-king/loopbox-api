package com.jammking.loopbox.application.service

import com.jammking.loopbox.adapter.out.persistence.jpa.JpaImageFileRepository
import com.jammking.loopbox.adapter.out.persistence.jpa.JpaImageGenerationTaskRepository
import com.jammking.loopbox.adapter.out.persistence.jpa.JpaImageRepository
import com.jammking.loopbox.adapter.out.persistence.jpa.JpaImageVersionRepository
import com.jammking.loopbox.adapter.out.persistence.jpa.JpaProjectRepository
import com.jammking.loopbox.application.port.`in`.HandleImageGenerationCallbackUseCase
import com.jammking.loopbox.application.port.out.ImageFileStorage
import com.jammking.loopbox.application.port.out.NotificationPort
import com.jammking.loopbox.domain.entity.image.Image
import com.jammking.loopbox.domain.entity.image.ImageConfig
import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.entity.image.ImageStatus
import com.jammking.loopbox.domain.entity.image.ImageVersionId
import com.jammking.loopbox.domain.entity.image.ImageVersionStatus
import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.music.MusicVersionId
import com.jammking.loopbox.domain.entity.project.Project
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.task.ExternalId
import com.jammking.loopbox.domain.entity.task.ImageAiProvider
import com.jammking.loopbox.domain.entity.task.ImageGenerationTask
import com.jammking.loopbox.domain.entity.task.ImageGenerationTaskStatus
import com.jammking.loopbox.domain.entity.user.UserId
import com.jammking.loopbox.domain.port.out.ImageGenerationTaskRepository
import com.jammking.loopbox.domain.port.out.ImageRepository
import com.jammking.loopbox.domain.port.out.ImageVersionRepository
import com.jammking.loopbox.domain.port.out.ProjectRepository
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@DataJpaTest
@ActiveProfiles("postgresql")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import(
    HandleImageGenerationCallbackService::class,
    JpaProjectRepository::class,
    JpaImageRepository::class,
    JpaImageVersionRepository::class,
    JpaImageGenerationTaskRepository::class,
    JpaImageFileRepository::class,
    HandleImageGenerationCallbackServiceTxTest.CallbackTestConfig::class
)
class HandleImageGenerationCallbackServiceTxTest {

    @Autowired
    private lateinit var service: HandleImageGenerationCallbackService

    @Autowired
    private lateinit var projectRepository: ProjectRepository

    @Autowired
    private lateinit var imageRepository: ImageRepository

    @Autowired
    private lateinit var versionRepository: ImageVersionRepository

    @Autowired
    private lateinit var taskRepository: ImageGenerationTaskRepository

    @Autowired
    private lateinit var entityManager: EntityManager

    @Test
    fun `handle failed callback persists state even when notification throws`() {
        val ownerId = UserId("user-1")
        val project = projectRepository.save(Project(ownerUserId = ownerId, title = "Project"))
        val image = Image(projectId = project.id)
        image.startVersionGeneration(ImageConfig())
        val savedImage = imageRepository.save(image)

        val task = ImageGenerationTask(
            imageId = savedImage.id,
            externalId = ExternalId("external-1"),
            status = ImageGenerationTaskStatus.GENERATING,
            provider = ImageAiProvider.REPLICATE_GOOGLE_IMAGEN_4
        )
        val savedTask = taskRepository.save(task)

        service.handle(
            HandleImageGenerationCallbackUseCase.Command(
                provider = ImageAiProvider.REPLICATE_GOOGLE_IMAGEN_4,
                status = HandleImageGenerationCallbackUseCase.Command.Status.FAILED,
                externalId = savedTask.externalId,
                images = null,
                message = "failed"
            )
        )

        entityManager.clear()
        val storedImage = imageRepository.findById(savedImage.id)
        val storedTask = taskRepository.findById(savedTask.id)
        val versions = versionRepository.findByImageId(savedImage.id)

        assertEquals(ImageStatus.FAILED, storedImage?.status)
        assertEquals(ImageGenerationTaskStatus.FAILED, storedTask?.status)
        assertEquals(ImageVersionStatus.GENERATION_FAILED, versions.first().status)
    }

    @TestConfiguration
    class CallbackTestConfig {
        @Bean
        fun notificationPort(): NotificationPort = object : NotificationPort {
            override fun notifyVersionGenerationCompleted(
                projectId: ProjectId,
                musicId: MusicId,
                versionIds: List<MusicVersionId>
            ) {
                throw IllegalStateException("notification failed")
            }

            override fun notifyVersionGenerationFailed(
                projectId: ProjectId,
                musicId: MusicId
            ) {
                throw IllegalStateException("notification failed")
            }

            override fun notifyImageVersionGenerationCompleted(
                projectId: ProjectId,
                imageId: ImageId,
                versionIds: List<ImageVersionId>
            ) {
                throw IllegalStateException("notification failed")
            }

            override fun notifyImageVersionGenerationFailed(
                projectId: ProjectId,
                imageId: ImageId
            ) {
                throw IllegalStateException("notification failed")
            }
        }

        @Bean
        fun imageFileStorage(): ImageFileStorage = object : ImageFileStorage {
            override fun saveFromRemoteUrl(
                remoteUrl: String,
                projectId: ProjectId,
                imageId: ImageId,
                versionId: ImageVersionId
            ): String {
                return "image.png"
            }
        }
    }
}
