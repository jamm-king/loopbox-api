package com.jammking.loopbox.application.service

import com.jammking.loopbox.adapter.out.persistence.jpa.JpaMusicGenerationTaskRepository
import com.jammking.loopbox.adapter.out.persistence.jpa.JpaMusicRepository
import com.jammking.loopbox.adapter.out.persistence.jpa.JpaMusicVersionRepository
import com.jammking.loopbox.adapter.out.persistence.jpa.JpaProjectRepository
import com.jammking.loopbox.application.port.`in`.MusicManagementUseCase
import com.jammking.loopbox.application.port.out.MusicAiClient
import com.jammking.loopbox.application.port.out.MusicAiRouter
import com.jammking.loopbox.domain.entity.music.Music
import com.jammking.loopbox.domain.entity.music.MusicConfig
import com.jammking.loopbox.domain.entity.music.MusicOperation
import com.jammking.loopbox.domain.entity.music.MusicStatus
import com.jammking.loopbox.domain.entity.project.Project
import com.jammking.loopbox.domain.entity.task.MusicAiProvider
import com.jammking.loopbox.domain.entity.user.UserId
import com.jammking.loopbox.domain.port.out.MusicRepository
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
    MusicManagementService::class,
    MusicFailureStateService::class,
    JpaProjectRepository::class,
    JpaMusicRepository::class,
    JpaMusicVersionRepository::class,
    JpaMusicGenerationTaskRepository::class,
    MusicManagementServiceTxTest.MusicAiTestConfig::class
)
class MusicManagementServiceTxTest(
) {
    @Autowired
    private lateinit var musicManagementService: MusicManagementService

    @Autowired
    private lateinit var projectRepository: ProjectRepository

    @Autowired
    private lateinit var musicRepository: MusicRepository

    @Autowired
    private lateinit var entityManager: EntityManager

    @Test
    fun `generateVersion persists failure state even when ai throws`() {
        val ownerId = UserId("user-1")
        val project = projectRepository.save(Project(ownerUserId = ownerId, title = "Project"))
        val music = musicRepository.save(Music(projectId = project.id))

        assertThrows(IllegalStateException::class.java) {
            musicManagementService.generateVersion(
                MusicManagementUseCase.GenerateVersionCommand(
                    userId = ownerId,
                    musicId = music.id,
                    config = MusicConfig(),
                    provider = MusicAiProvider.SUNO
                )
            )
        }

        entityManager.clear()
        val persisted = musicRepository.findById(music.id)
        assertEquals(MusicStatus.FAILED, persisted?.status)
        assertEquals(MusicOperation.GENERATE_VERSION, persisted?.lastOperation)
    }

    @TestConfiguration
    class MusicAiTestConfig {
        @Bean
        fun musicAiRouter(): MusicAiRouter = object : MusicAiRouter {
            override fun getClient(provider: MusicAiProvider): MusicAiClient = object : MusicAiClient {
                override val provider: MusicAiProvider = provider

                override fun generate(command: MusicAiClient.GenerateCommand): MusicAiClient.GenerateResult {
                    throw IllegalStateException("boom")
                }

                override fun expand(command: MusicAiClient.ExpandCommand): MusicAiClient.ExpandResult {
                    throw IllegalStateException("boom")
                }
            }
        }
    }
}
