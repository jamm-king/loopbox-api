package com.jammking.loopbox.adapter.out.persistence

import com.jammking.loopbox.adapter.out.persistence.inmemeory.InMemoryAudioFileRepository
import com.jammking.loopbox.adapter.out.persistence.inmemeory.InMemoryImageFileRepository
import com.jammking.loopbox.adapter.out.persistence.inmemeory.InMemoryImageGenerationTaskRepository
import com.jammking.loopbox.adapter.out.persistence.inmemeory.InMemoryImageRepository
import com.jammking.loopbox.adapter.out.persistence.inmemeory.InMemoryImageVersionRepository
import com.jammking.loopbox.adapter.out.persistence.inmemeory.InMemoryMusicGenerationTaskRepository
import com.jammking.loopbox.adapter.out.persistence.inmemeory.InMemoryMusicRepository
import com.jammking.loopbox.adapter.out.persistence.inmemeory.InMemoryMusicVersionRepository
import com.jammking.loopbox.adapter.out.persistence.inmemeory.InMemoryProjectRepository
import com.jammking.loopbox.adapter.out.persistence.inmemeory.InMemoryRefreshTokenRepository
import com.jammking.loopbox.adapter.out.persistence.inmemeory.InMemoryUserRepository
import com.jammking.loopbox.adapter.out.persistence.inmemeory.InMemoryVideoFileRepository
import com.jammking.loopbox.adapter.out.persistence.inmemeory.InMemoryVideoRepository
import com.jammking.loopbox.domain.port.out.AudioFileRepository
import com.jammking.loopbox.domain.port.out.ImageFileRepository
import com.jammking.loopbox.domain.port.out.ImageGenerationTaskRepository
import com.jammking.loopbox.domain.port.out.ImageRepository
import com.jammking.loopbox.domain.port.out.ImageVersionRepository
import com.jammking.loopbox.domain.port.out.MusicGenerationTaskRepository
import com.jammking.loopbox.domain.port.out.MusicRepository
import com.jammking.loopbox.domain.port.out.MusicVersionRepository
import com.jammking.loopbox.domain.port.out.ProjectRepository
import com.jammking.loopbox.domain.port.out.RefreshTokenRepository
import com.jammking.loopbox.domain.port.out.UserRepository
import com.jammking.loopbox.domain.port.out.VideoFileRepository
import com.jammking.loopbox.domain.port.out.VideoRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

class PersistenceProfileSelectionTest {
    private val contextRunner = ApplicationContextRunner()
        .withUserConfiguration(PersistenceComponentScanConfig::class.java)
        .withPropertyValues("spring.profiles.active=inmemory")

    @Test
    fun `inmemory profile wires only in-memory repositories`() {
        contextRunner.run { context ->
            assertSingleBean(context, ProjectRepository::class.java, InMemoryProjectRepository::class.java)
            assertSingleBean(context, MusicRepository::class.java, InMemoryMusicRepository::class.java)
            assertSingleBean(context, MusicVersionRepository::class.java, InMemoryMusicVersionRepository::class.java)
            assertSingleBean(context, ImageRepository::class.java, InMemoryImageRepository::class.java)
            assertSingleBean(context, ImageVersionRepository::class.java, InMemoryImageVersionRepository::class.java)
            assertSingleBean(context, AudioFileRepository::class.java, InMemoryAudioFileRepository::class.java)
            assertSingleBean(context, VideoRepository::class.java, InMemoryVideoRepository::class.java)
            assertSingleBean(context, VideoFileRepository::class.java, InMemoryVideoFileRepository::class.java)
            assertSingleBean(context, ImageFileRepository::class.java, InMemoryImageFileRepository::class.java)
            assertSingleBean(context, RefreshTokenRepository::class.java, InMemoryRefreshTokenRepository::class.java)
            assertSingleBean(context, UserRepository::class.java, InMemoryUserRepository::class.java)
            assertSingleBean(
                context,
                ImageGenerationTaskRepository::class.java,
                InMemoryImageGenerationTaskRepository::class.java
            )
            assertSingleBean(
                context,
                MusicGenerationTaskRepository::class.java,
                InMemoryMusicGenerationTaskRepository::class.java
            )
        }
    }

    private fun <T : Any> assertSingleBean(
        context: ApplicationContext,
        type: Class<T>,
        expectedType: Class<out T>
    ) {
        val beans = context.getBeansOfType(type)
        assertThat(beans).hasSize(1)
        assertThat(beans.values.first()).isInstanceOf(expectedType)
    }

    @Configuration
    @ComponentScan("com.jammking.loopbox.adapter.out.persistence")
    class PersistenceComponentScanConfig
}
