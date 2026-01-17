package com.jammking.loopbox.application.service

import com.jammking.loopbox.adapter.out.persistence.jpa.JpaAudioFileRepository
import com.jammking.loopbox.adapter.out.persistence.jpa.JpaMusicGenerationTaskRepository
import com.jammking.loopbox.adapter.out.persistence.jpa.JpaMusicRepository
import com.jammking.loopbox.adapter.out.persistence.jpa.JpaMusicVersionRepository
import com.jammking.loopbox.adapter.out.persistence.jpa.JpaProjectRepository
import com.jammking.loopbox.application.port.`in`.HandleMusicGenerationCallbackUseCase
import com.jammking.loopbox.application.port.out.MusicFileStorage
import com.jammking.loopbox.application.port.out.NotificationPort
import com.jammking.loopbox.domain.entity.music.Music
import com.jammking.loopbox.domain.entity.music.MusicConfig
import com.jammking.loopbox.domain.entity.music.MusicStatus
import com.jammking.loopbox.domain.entity.music.MusicVersionStatus
import com.jammking.loopbox.domain.entity.project.Project
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.task.ExternalId
import com.jammking.loopbox.domain.entity.task.MusicAiProvider
import com.jammking.loopbox.domain.entity.task.MusicGenerationTask
import com.jammking.loopbox.domain.entity.task.MusicGenerationTaskStatus
import com.jammking.loopbox.domain.entity.user.UserId
import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.entity.image.ImageVersionId
import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.music.MusicVersionId
import com.jammking.loopbox.domain.port.out.MusicGenerationTaskRepository
import com.jammking.loopbox.domain.port.out.MusicRepository
import com.jammking.loopbox.domain.port.out.MusicVersionRepository
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
    HandleMusicGenerationCallbackService::class,
    JpaProjectRepository::class,
    JpaMusicRepository::class,
    JpaMusicVersionRepository::class,
    JpaMusicGenerationTaskRepository::class,
    JpaAudioFileRepository::class,
    HandleMusicGenerationCallbackServiceTxTest.CallbackTestConfig::class
)
class HandleMusicGenerationCallbackServiceTxTest {

    @Autowired
    private lateinit var service: HandleMusicGenerationCallbackService

    @Autowired
    private lateinit var projectRepository: ProjectRepository

    @Autowired
    private lateinit var musicRepository: MusicRepository

    @Autowired
    private lateinit var versionRepository: MusicVersionRepository

    @Autowired
    private lateinit var taskRepository: MusicGenerationTaskRepository

    @Autowired
    private lateinit var entityManager: EntityManager

    @Test
    fun `handle failed callback persists state even when notification throws`() {
        val ownerId = UserId("user-1")
        val project = projectRepository.save(Project(ownerUserId = ownerId, title = "Project"))
        val music = Music(projectId = project.id)
        music.startVersionGeneration(MusicConfig())
        val savedMusic = musicRepository.save(music)

        val task = MusicGenerationTask(
            musicId = savedMusic.id,
            externalId = ExternalId("external-1"),
            status = MusicGenerationTaskStatus.GENERATING,
            provider = MusicAiProvider.SUNO
        )
        val savedTask = taskRepository.save(task)

        service.handle(
            HandleMusicGenerationCallbackUseCase.Command(
                provider = MusicAiProvider.SUNO,
                status = HandleMusicGenerationCallbackUseCase.Command.Status.FAILED,
                externalId = savedTask.externalId,
                tracks = null,
                message = "failed"
            )
        )

        entityManager.clear()
        val storedMusic = musicRepository.findById(savedMusic.id)
        val storedTask = taskRepository.findById(savedTask.id)
        val versions = versionRepository.findByMusicId(savedMusic.id)

        assertEquals(MusicStatus.FAILED, storedMusic?.status)
        assertEquals(MusicGenerationTaskStatus.FAILED, storedTask?.status)
        assertEquals(MusicVersionStatus.GENERATION_FAILED, versions.first().status)
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
        fun musicFileStorage(): MusicFileStorage = object : MusicFileStorage {
            override fun saveFromRemoteUrl(
                remoteUrl: String,
                projectId: ProjectId,
                musicId: MusicId,
                versionId: MusicVersionId
            ): String {
                return "music.mp3"
            }
        }
    }
}
