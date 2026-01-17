package com.jammking.loopbox.application.service

import com.jammking.loopbox.adapter.out.persistence.jpa.JpaAudioFileRepository
import com.jammking.loopbox.adapter.out.persistence.jpa.JpaImageFileRepository
import com.jammking.loopbox.adapter.out.persistence.jpa.JpaImageRepository
import com.jammking.loopbox.adapter.out.persistence.jpa.JpaImageVersionRepository
import com.jammking.loopbox.adapter.out.persistence.jpa.JpaMusicRepository
import com.jammking.loopbox.adapter.out.persistence.jpa.JpaMusicVersionRepository
import com.jammking.loopbox.adapter.out.persistence.jpa.JpaProjectRepository
import com.jammking.loopbox.adapter.out.persistence.jpa.JpaVideoRepository
import com.jammking.loopbox.application.port.out.VideoFileStorage
import com.jammking.loopbox.application.port.out.VideoRenderClient
import com.jammking.loopbox.domain.entity.project.Project
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.user.UserId
import com.jammking.loopbox.domain.entity.video.Video
import com.jammking.loopbox.domain.entity.video.VideoId
import com.jammking.loopbox.domain.entity.video.VideoStatus
import com.jammking.loopbox.domain.port.out.ProjectRepository
import com.jammking.loopbox.domain.port.out.VideoRepository
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
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
    VideoManagementService::class,
    VideoRenderFailureService::class,
    JpaProjectRepository::class,
    JpaVideoRepository::class,
    JpaMusicRepository::class,
    JpaMusicVersionRepository::class,
    JpaImageRepository::class,
    JpaImageVersionRepository::class,
    JpaAudioFileRepository::class,
    JpaImageFileRepository::class,
    VideoManagementServiceTxTest.RenderTestConfig::class
)
class VideoManagementServiceTxTest {

    @Autowired
    private lateinit var videoManagementService: VideoManagementService

    @Autowired
    private lateinit var projectRepository: ProjectRepository

    @Autowired
    private lateinit var videoRepository: VideoRepository

    @Autowired
    private lateinit var entityManager: EntityManager

    @Test
    fun `requestRender persists failure state when render client throws`() {
        val ownerId = UserId("user-1")
        val project = projectRepository.save(Project(ownerUserId = ownerId, title = "Project"))
        videoRepository.save(Video(projectId = project.id))

        assertThrows(IllegalStateException::class.java) {
            videoManagementService.requestRender(ownerId, project.id)
        }

        entityManager.clear()
        val stored = videoRepository.findByProjectId(project.id)
        assertEquals(VideoStatus.FAILED, stored?.status)
    }

    @TestConfiguration
    class RenderTestConfig {
        @Bean
        fun videoFileStorage(): VideoFileStorage = object : VideoFileStorage {
            override fun prepareRenderPath(projectId: ProjectId, videoId: VideoId): String {
                return "video.mp4"
            }
        }

        @Bean
        fun videoRenderClient(): VideoRenderClient = object : VideoRenderClient {
            override fun requestRender(command: VideoRenderClient.RenderCommand) {
                throw IllegalStateException("render failed")
            }
        }
    }
}
