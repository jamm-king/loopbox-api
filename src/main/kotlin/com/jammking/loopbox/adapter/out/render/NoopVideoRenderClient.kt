package com.jammking.loopbox.adapter.out.render

import com.jammking.loopbox.application.port.out.VideoRenderClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class NoopVideoRenderClient: VideoRenderClient {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun requestRender(command: VideoRenderClient.RenderCommand) {
        log.info(
            "Queued video render (noop): projectId={}, videoId={}, segments={}, imageGroups={}, outputPath={}",
            command.projectId.value,
            command.videoId.value,
            command.segments.size,
            command.imageGroups.size,
            command.outputPath
        )
    }
}
