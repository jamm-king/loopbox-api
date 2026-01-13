package com.jammking.loopbox.adapter.out.render

import com.jammking.loopbox.application.port.out.VideoRenderClient
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.video.VideoId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FfmpegRenderPlannerTest {

    private val planner = FfmpegRenderPlanner(ffmpegPath = "ffmpeg")

    @Test
    fun `buildImageIntervals fills gaps with previous image`() {
        val command = VideoRenderClient.RenderCommand(
            projectId = ProjectId("project-1"),
            videoId = VideoId("video-1"),
            outputPath = "output.mp4",
            segments = listOf(
                VideoRenderClient.RenderSegment("m1", "a1.mp3", 2),
                VideoRenderClient.RenderSegment("m2", "a2.mp3", 3),
                VideoRenderClient.RenderSegment("m3", "a3.mp3", 4)
            ),
            imageGroups = listOf(
                VideoRenderClient.RenderImageGroup("i1", "/a.png", 0, 0),
                VideoRenderClient.RenderImageGroup("i2", "/b.png", 2, 2)
            )
        )

        val intervals = planner.buildImageIntervals(command)

        assertEquals(
            listOf(
                FfmpegRenderPlanner.ImageInterval("/a.png", 5),
                FfmpegRenderPlanner.ImageInterval("/b.png", 4)
            ),
            intervals
        )
    }

    @Test
    fun `buildImageIntervals keeps leading gap as black`() {
        val command = VideoRenderClient.RenderCommand(
            projectId = ProjectId("project-1"),
            videoId = VideoId("video-1"),
            outputPath = "output.mp4",
            segments = listOf(
                VideoRenderClient.RenderSegment("m1", "a1.mp3", 2),
                VideoRenderClient.RenderSegment("m2", "a2.mp3", 3)
            ),
            imageGroups = listOf(
                VideoRenderClient.RenderImageGroup("i1", "/b.png", 1, 1)
            )
        )

        val intervals = planner.buildImageIntervals(command)

        assertEquals(
            listOf(
                FfmpegRenderPlanner.ImageInterval(null, 2),
                FfmpegRenderPlanner.ImageInterval("/b.png", 3)
            ),
            intervals
        )
    }

    @Test
    fun `build uses escaped commas in scale filter`() {
        val command = VideoRenderClient.RenderCommand(
            projectId = ProjectId("project-1"),
            videoId = VideoId("video-1"),
            outputPath = "output.mp4",
            segments = listOf(
                VideoRenderClient.RenderSegment("m1", "a1.mp3", 2)
            ),
            imageGroups = listOf(
                VideoRenderClient.RenderImageGroup("i1", "/a.png", 0, 0)
            )
        )

        val plan = planner.build(command)
        val filterArgIndex = plan.commandLine.indexOf("-filter_complex")
        val filter = plan.commandLine[filterArgIndex + 1]

        assertEquals(true, filter.contains("scale=if(gt(a\\,1.777778)\\,-1\\,1920)"))
    }
}
