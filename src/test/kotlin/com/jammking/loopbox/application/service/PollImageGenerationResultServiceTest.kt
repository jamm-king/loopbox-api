package com.jammking.loopbox.application.service

import com.jammking.loopbox.application.port.`in`.HandleImageGenerationCallbackUseCase
import com.jammking.loopbox.application.port.out.ImageAiClient
import com.jammking.loopbox.application.port.out.ImageAiRouter
import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.entity.task.ExternalId
import com.jammking.loopbox.domain.entity.task.ImageAiProvider
import com.jammking.loopbox.domain.entity.task.ImageGenerationTask
import com.jammking.loopbox.domain.entity.task.ImageGenerationTaskStatus
import com.jammking.loopbox.domain.port.out.ImageGenerationTaskRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class PollImageGenerationResultServiceTest {

    @Mock
    private lateinit var taskRepository: ImageGenerationTaskRepository

    @Mock
    private lateinit var imageAiRouter: ImageAiRouter

    @Mock
    private lateinit var handleImageGenerationCallbackUseCase: HandleImageGenerationCallbackUseCase

    @Mock
    private lateinit var imageAiClient: ImageAiClient

    private lateinit var service: PollImageGenerationResultService

    @BeforeEach
    fun setUp() {
        service = PollImageGenerationResultService(
            taskRepository = taskRepository,
            imageAiRouter = imageAiRouter,
            handleImageGenerationCallbackUseCase = handleImageGenerationCallbackUseCase,
            maxPerRun = 20,
            maxAttempts = 5,
            minIntervalSeconds = 60
        )
    }

    @Test
    fun `poll should fail task when max attempts exceeded`() {
        val task = ImageGenerationTask(
            imageId = ImageId("image-1"),
            externalId = ExternalId("external-1"),
            status = ImageGenerationTaskStatus.GENERATING,
            provider = ImageAiProvider.REPLICATE_GOOGLE_IMAGEN_4,
            pollCount = 5,
            updatedAt = Instant.now().minusSeconds(120)
        )
        whenever(
            taskRepository.findByStatusAndProviderAndUpdatedBefore(
                eq(ImageGenerationTaskStatus.GENERATING),
                eq(ImageAiProvider.REPLICATE_GOOGLE_IMAGEN_4),
                any()
            )
        ).thenReturn(listOf(task))

        service.poll()

        val commandCaptor = argumentCaptor<HandleImageGenerationCallbackUseCase.Command>()
        verify(handleImageGenerationCallbackUseCase).handle(commandCaptor.capture())
        assertEquals(HandleImageGenerationCallbackUseCase.Command.Status.FAILED, commandCaptor.firstValue.status)
        verify(imageAiRouter, never()).getClient(any())
        verify(taskRepository, never()).save(any())
    }

    @Test
    fun `poll should fetch result and map completed status`() {
        val task = ImageGenerationTask(
            imageId = ImageId("image-1"),
            externalId = ExternalId("external-1"),
            status = ImageGenerationTaskStatus.GENERATING,
            provider = ImageAiProvider.REPLICATE_GOOGLE_IMAGEN_4,
            updatedAt = Instant.now().minusSeconds(120)
        )
        whenever(
            taskRepository.findByStatusAndProviderAndUpdatedBefore(
                eq(ImageGenerationTaskStatus.GENERATING),
                eq(ImageAiProvider.REPLICATE_GOOGLE_IMAGEN_4),
                any()
            )
        ).thenReturn(listOf(task))
        whenever(imageAiRouter.getClient(ImageAiProvider.REPLICATE_GOOGLE_IMAGEN_4)).thenReturn(imageAiClient)
        whenever(imageAiClient.fetchResult(any())).thenReturn(
            ImageAiClient.FetchResult(
                status = ImageAiClient.FetchResult.FetchStatus.COMPLETED,
                images = listOf(ImageAiClient.ImageAsset(remoteUrl = "https://example.com/1.jpg"))
            )
        )
        whenever(taskRepository.save(any())).thenAnswer { it.arguments[0] }

        service.poll()

        val savedTaskCaptor = argumentCaptor<ImageGenerationTask>()
        verify(taskRepository, times(1)).save(savedTaskCaptor.capture())
        assertEquals(1, savedTaskCaptor.firstValue.pollCount)

        val commandCaptor = argumentCaptor<HandleImageGenerationCallbackUseCase.Command>()
        verify(handleImageGenerationCallbackUseCase).handle(commandCaptor.capture())
        assertEquals(HandleImageGenerationCallbackUseCase.Command.Status.COMPLETED, commandCaptor.firstValue.status)
        assertEquals("https://example.com/1.jpg", commandCaptor.firstValue.images?.first()?.remoteUrl)
    }

    @Test
    fun `poll should respect max per run`() {
        service = PollImageGenerationResultService(
            taskRepository = taskRepository,
            imageAiRouter = imageAiRouter,
            handleImageGenerationCallbackUseCase = handleImageGenerationCallbackUseCase,
            maxPerRun = 1,
            maxAttempts = 5,
            minIntervalSeconds = 60
        )
        val olderTask = ImageGenerationTask(
            imageId = ImageId("image-1"),
            externalId = ExternalId("external-1"),
            status = ImageGenerationTaskStatus.GENERATING,
            provider = ImageAiProvider.REPLICATE_GOOGLE_IMAGEN_4,
            updatedAt = Instant.now().minusSeconds(300)
        )
        val newerTask = ImageGenerationTask(
            imageId = ImageId("image-2"),
            externalId = ExternalId("external-2"),
            status = ImageGenerationTaskStatus.GENERATING,
            provider = ImageAiProvider.REPLICATE_GOOGLE_IMAGEN_4,
            updatedAt = Instant.now().minusSeconds(120)
        )
        whenever(
            taskRepository.findByStatusAndProviderAndUpdatedBefore(
                eq(ImageGenerationTaskStatus.GENERATING),
                eq(ImageAiProvider.REPLICATE_GOOGLE_IMAGEN_4),
                any()
            )
        ).thenReturn(listOf(newerTask, olderTask))
        whenever(imageAiRouter.getClient(ImageAiProvider.REPLICATE_GOOGLE_IMAGEN_4)).thenReturn(imageAiClient)
        whenever(imageAiClient.fetchResult(any())).thenReturn(
            ImageAiClient.FetchResult(
                status = ImageAiClient.FetchResult.FetchStatus.GENERATING,
                images = null
            )
        )
        whenever(taskRepository.save(any())).thenAnswer { it.arguments[0] }

        service.poll()

        verify(imageAiClient, times(1)).fetchResult(any())
        val savedTaskCaptor = argumentCaptor<ImageGenerationTask>()
        verify(taskRepository, times(1)).save(savedTaskCaptor.capture())
        assertEquals("image-1", savedTaskCaptor.firstValue.imageId.value)
    }

    @Test
    fun `poll should increment poll count across runs`() {
        val oldTime = Instant.now().minusSeconds(120)
        val firstTask = ImageGenerationTask(
            imageId = ImageId("image-1"),
            externalId = ExternalId("external-1"),
            status = ImageGenerationTaskStatus.GENERATING,
            provider = ImageAiProvider.REPLICATE_GOOGLE_IMAGEN_4,
            updatedAt = oldTime
        )
        val secondTask = ImageGenerationTask(
            imageId = ImageId("image-1"),
            externalId = ExternalId("external-1"),
            status = ImageGenerationTaskStatus.GENERATING,
            provider = ImageAiProvider.REPLICATE_GOOGLE_IMAGEN_4,
            pollCount = 1,
            updatedAt = oldTime
        )

        whenever(
            taskRepository.findByStatusAndProviderAndUpdatedBefore(
                eq(ImageGenerationTaskStatus.GENERATING),
                eq(ImageAiProvider.REPLICATE_GOOGLE_IMAGEN_4),
                any()
            )
        ).thenReturn(listOf(firstTask), listOf(secondTask))
        whenever(imageAiRouter.getClient(ImageAiProvider.REPLICATE_GOOGLE_IMAGEN_4)).thenReturn(imageAiClient)
        whenever(imageAiClient.fetchResult(any())).thenReturn(
            ImageAiClient.FetchResult(
                status = ImageAiClient.FetchResult.FetchStatus.GENERATING,
                images = null
            )
        )
        whenever(taskRepository.save(any())).thenAnswer { it.arguments[0] }

        service.poll()
        service.poll()

        val savedTaskCaptor = argumentCaptor<ImageGenerationTask>()
        verify(taskRepository, times(2)).save(savedTaskCaptor.capture())
        assertEquals(1, savedTaskCaptor.firstValue.pollCount)
        assertEquals(2, savedTaskCaptor.secondValue.pollCount)
    }
}
