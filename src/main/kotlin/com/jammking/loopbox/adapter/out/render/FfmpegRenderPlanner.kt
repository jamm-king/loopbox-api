package com.jammking.loopbox.adapter.out.render

import com.jammking.loopbox.application.port.out.VideoRenderClient
import java.util.Locale

class FfmpegRenderPlanner(
    private val ffmpegPath: String,
    private val width: Int = 1920,
    private val height: Int = 1080,
    private val fps: Int = 30,
    private val crf: Int = 18,
    private val preset: String = "medium",
    private val audioBitrateKbps: Int = 320,
    private val audioSampleRate: Int = 48000
) {
    data class ImageInterval(val imagePath: String?, val durationSeconds: Int)

    data class AudioSegment(val audioPath: String, val durationSeconds: Int)

    data class Plan(
        val commandLine: List<String>,
        val imageIntervals: List<ImageInterval>,
        val audioSegments: List<AudioSegment>
    )

    fun build(command: VideoRenderClient.RenderCommand): Plan {
        val imageIntervals = buildImageIntervals(command)
        val audioSegments = command.segments.map {
            AudioSegment(audioPath = it.audioPath, durationSeconds = it.durationSeconds)
        }

        return Plan(
            commandLine = buildCommandLine(command.outputPath, imageIntervals, audioSegments),
            imageIntervals = imageIntervals,
            audioSegments = audioSegments
        )
    }

    internal fun buildImageIntervals(command: VideoRenderClient.RenderCommand): List<ImageInterval> {
        val segmentCount = command.segments.size
        require(segmentCount > 0) { "Render requires at least one segment." }

        val imagePaths = arrayOfNulls<String>(segmentCount)
        command.imageGroups.forEach { group ->
            for (index in group.segmentIndexStart..group.segmentIndexEnd) {
                imagePaths[index] = group.imagePath
            }
        }

        var lastImagePath: String? = null
        for (index in 0 until segmentCount) {
            val current = imagePaths[index]
            if (current != null) {
                lastImagePath = current
            } else if (lastImagePath != null) {
                imagePaths[index] = lastImagePath
            }
        }

        val durations = command.segments.map { it.durationSeconds }
        val intervals = mutableListOf<ImageInterval>()
        var currentPath = imagePaths[0]
        var currentDuration = durations[0]
        for (index in 1 until segmentCount) {
            if (imagePaths[index] == currentPath) {
                currentDuration += durations[index]
            } else {
                intervals.add(ImageInterval(currentPath, currentDuration))
                currentPath = imagePaths[index]
                currentDuration = durations[index]
            }
        }
        intervals.add(ImageInterval(currentPath, currentDuration))

        return intervals
    }

    private fun buildCommandLine(
        outputPath: String,
        imageIntervals: List<ImageInterval>,
        audioSegments: List<AudioSegment>
    ): List<String> {
        val args = mutableListOf(ffmpegPath, "-y")

        imageIntervals.forEach { interval ->
            val duration = interval.durationSeconds.toString()
            if (interval.imagePath == null) {
                args.addAll(
                    listOf(
                        "-f", "lavfi",
                        "-t", duration,
                        "-i", "color=c=black:s=${width}x${height}:r=${fps}"
                    )
                )
            } else {
                args.addAll(
                    listOf(
                        "-loop", "1",
                        "-t", duration,
                        "-i", interval.imagePath
                    )
                )
            }
        }

        audioSegments.forEach { segment ->
            args.addAll(listOf("-i", segment.audioPath))
        }

        val filter = buildFilter(imageIntervals, audioSegments)
        args.addAll(
            listOf(
                "-filter_complex", filter,
                "-map", "[vout]",
                "-map", "[aout]",
                "-c:v", "libx264",
                "-preset", preset,
                "-crf", crf.toString(),
                "-pix_fmt", "yuv420p",
                "-profile:v", "high",
                "-level", "4.2",
                "-r", fps.toString(),
                "-c:a", "aac",
                "-b:a", "${audioBitrateKbps}k",
                "-ar", audioSampleRate.toString(),
                "-movflags", "+faststart",
                outputPath
            )
        )

        return args
    }

    private fun buildFilter(
        imageIntervals: List<ImageInterval>,
        audioSegments: List<AudioSegment>
    ): String {
        val aspect = String.format(Locale.US, "%.6f", width.toDouble() / height)
        val scaleFilter = "scale=if(gt(a\\,$aspect)\\,-1\\,$width):if(gt(a\\,$aspect)\\,$height\\,-1),crop=$width:$height"
        val filters = mutableListOf<String>()

        imageIntervals.forEachIndexed { index, interval ->
            val trimFilter = "trim=duration=${interval.durationSeconds},setpts=PTS-STARTPTS,setsar=1"
            val filter = if (interval.imagePath == null) {
                "[$index:v]$trimFilter[v$index]"
            } else {
                "[$index:v]$scaleFilter,$trimFilter[v$index]"
            }
            filters.add(filter)
        }

        val videoInputs = imageIntervals.indices.joinToString("") { "[v$it]" }
        filters.add("${videoInputs}concat=n=${imageIntervals.size}:v=1:a=0[vout]")

        val audioOffset = imageIntervals.size
        audioSegments.forEachIndexed { index, segment ->
            val inputIndex = audioOffset + index
            filters.add("[$inputIndex:a]atrim=duration=${segment.durationSeconds},asetpts=PTS-STARTPTS[a$index]")
        }

        val audioInputs = audioSegments.indices.joinToString("") { "[a$it]" }
        filters.add("${audioInputs}concat=n=${audioSegments.size}:v=0:a=1[aout]")

        return filters.joinToString(";")
    }
}
