package com.jammking.loopbox.adapter.out.render

import com.jammking.loopbox.application.port.`in`.HandleVideoRenderCallbackUseCase
import com.jammking.loopbox.application.port.out.VideoRenderClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

@Component
class FfmpegVideoRenderClient(
    private val handleVideoRenderCallbackUseCase: HandleVideoRenderCallbackUseCase,
    @Value("\${loopbox.render.ffmpeg-path:ffmpeg}")
    private val ffmpegPath: String
): VideoRenderClient {

    private val log = LoggerFactory.getLogger(javaClass)
    private val planner = FfmpegRenderPlanner(ffmpegPath = ffmpegPath)
    private val threadId = AtomicInteger(0)
    private val executor = Executors.newCachedThreadPool { runnable ->
        Thread(runnable, "video-render-${threadId.incrementAndGet()}")
    }

    override fun requestRender(command: VideoRenderClient.RenderCommand) {
        executor.execute { render(command) }
    }

    private fun render(command: VideoRenderClient.RenderCommand) {
        log.info(
            "Start video render: projectId={}, videoId={}, segments={}, imageGroups={}",
            command.projectId.value,
            command.videoId.value,
            command.segments.size,
            command.imageGroups.size
        )

        handleVideoRenderCallbackUseCase.handle(
            HandleVideoRenderCallbackUseCase.Command(
                projectId = command.projectId,
                status = HandleVideoRenderCallbackUseCase.Status.RENDERING,
                message = "ffmpeg render started"
            )
        )

        try {
            val plan = planner.build(command)
            val outputBuffer = StringBuilder()
            val process = ProcessBuilder(plan.commandLine)
                .redirectErrorStream(true)
                .start()

            process.inputStream.bufferedReader().forEachLine { line ->
                if (outputBuffer.length < 4000) {
                    outputBuffer.append(line).append('\n')
                }
            }

            val exitCode = process.waitFor()
            if (exitCode == 0) {
                handleVideoRenderCallbackUseCase.handle(
                    HandleVideoRenderCallbackUseCase.Command(
                        projectId = command.projectId,
                        status = HandleVideoRenderCallbackUseCase.Status.COMPLETED,
                        outputPath = command.outputPath
                    )
                )
                log.info(
                    "Video render completed: projectId={}, videoId={}, outputPath={}",
                    command.projectId.value,
                    command.videoId.value,
                    command.outputPath
                )
                return
            }

            val message = outputBuffer.toString().trim()
                .ifBlank { "ffmpeg exit code $exitCode" }
            log.warn(
                "Video render failed: projectId={}, videoId={}, exitCode={}",
                command.projectId.value,
                command.videoId.value,
                exitCode
            )
            handleVideoRenderCallbackUseCase.handle(
                HandleVideoRenderCallbackUseCase.Command(
                    projectId = command.projectId,
                    status = HandleVideoRenderCallbackUseCase.Status.FAILED,
                    message = message
                )
            )
        } catch (e: Exception) {
            log.error(
                "Video render failed: projectId={}, videoId={}, reason={}",
                command.projectId.value,
                command.videoId.value,
                e.message,
                e
            )
            handleVideoRenderCallbackUseCase.handle(
                HandleVideoRenderCallbackUseCase.Command(
                    projectId = command.projectId,
                    status = HandleVideoRenderCallbackUseCase.Status.FAILED,
                    message = e.message ?: "ffmpeg render failed"
                )
            )
        }
    }
}
