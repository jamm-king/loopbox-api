package com.jammking.loopbox.application.service

import com.jammking.loopbox.application.port.`in`.HandleImageGenerationCallbackUseCase
import com.jammking.loopbox.application.port.`in`.PollImageGenerationResultUseCase
import com.jammking.loopbox.application.port.out.ImageAiClient
import com.jammking.loopbox.application.port.out.ImageAiRouter
import com.jammking.loopbox.domain.entity.task.ImageAiProvider
import com.jammking.loopbox.domain.entity.task.ImageGenerationTaskStatus
import com.jammking.loopbox.domain.port.out.ImageGenerationTaskRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
@Transactional
class PollImageGenerationResultService(
    private val taskRepository: ImageGenerationTaskRepository,
    private val imageAiRouter: ImageAiRouter,
    private val handleImageGenerationCallbackUseCase: HandleImageGenerationCallbackUseCase,
    @Value("\${loopbox.replicate.polling.max-per-run:20}")
    private val maxPerRun: Int,
    @Value("\${loopbox.replicate.polling.max-attempts:5}")
    private val maxAttempts: Int,
    @Value("\${loopbox.replicate.polling.min-interval-seconds:60}")
    private val minIntervalSeconds: Long
): PollImageGenerationResultUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun poll() {
        val cutoff = Instant.now().minusSeconds(minIntervalSeconds)
        val tasks = taskRepository.findByStatusAndProviderAndUpdatedBefore(
            status = ImageGenerationTaskStatus.GENERATING,
            provider = ImageAiProvider.REPLICATE_GOOGLE_IMAGEN_4,
            before = cutoff
        ).sortedBy { it.updatedAt }

        if (tasks.isEmpty()) return

        tasks.take(maxPerRun).forEach { task ->
            try {
                if (task.pollCount >= maxAttempts) {
                    val command = HandleImageGenerationCallbackUseCase.Command(
                        provider = task.provider,
                        status = HandleImageGenerationCallbackUseCase.Command.Status.FAILED,
                        externalId = task.externalId,
                        images = null,
                        message = "Polling exceeded max attempts: $maxAttempts"
                    )
                    handleImageGenerationCallbackUseCase.handle(command)
                    return@forEach
                }

                task.incrementPollCount()
                taskRepository.save(task)

                val client = imageAiRouter.getClient(task.provider)
                val result = client.fetchResult(
                    ImageAiClient.FetchResultCommand(externalId = task.externalId)
                )

                val status = when(result.status) {
                    ImageAiClient.FetchResult.FetchStatus.COMPLETED ->
                        HandleImageGenerationCallbackUseCase.Command.Status.COMPLETED
                    ImageAiClient.FetchResult.FetchStatus.FAILED ->
                        HandleImageGenerationCallbackUseCase.Command.Status.FAILED
                    ImageAiClient.FetchResult.FetchStatus.GENERATING ->
                        HandleImageGenerationCallbackUseCase.Command.Status.GENERATING
                    ImageAiClient.FetchResult.FetchStatus.UNKNOWN ->
                        HandleImageGenerationCallbackUseCase.Command.Status.UNKNOWN
                }

                val command = HandleImageGenerationCallbackUseCase.Command(
                    provider = task.provider,
                    status = status,
                    externalId = task.externalId,
                    images = result.images?.map {
                        HandleImageGenerationCallbackUseCase.Command.ImageAsset(it.remoteUrl)
                    },
                    message = result.message
                )

                handleImageGenerationCallbackUseCase.handle(command)
            } catch(e: Exception) {
                log.warn(
                    "Failed to poll image generation result: taskId={}, externalId={}, reason={}",
                    task.id.value, task.externalId.value, e.message, e
                )
            }
        }
    }
}
