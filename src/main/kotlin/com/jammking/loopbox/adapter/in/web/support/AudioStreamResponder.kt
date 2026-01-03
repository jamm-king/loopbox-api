package com.jammking.loopbox.adapter.`in`.web.support

import com.jammking.loopbox.application.port.`in`.GetMusicVersionAudioUseCase
import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import java.io.BufferedInputStream
import java.io.IOException
import java.nio.file.Files

@Component
class AudioStreamResponder {

    private val log = LoggerFactory.getLogger(javaClass)

    fun respond(
        target: GetMusicVersionAudioUseCase.AudioStreamTarget,
        headers: HttpHeaders
    ): ResponseEntity<StreamingResponseBody> {
        val path = target.path
        val totalLength = target.contentLength
        val mediaType = parseMediaTypeOrOctet(target.contentType)

        val range = headers.range.firstOrNull()
        return if (range == null) {
            okFull(path, totalLength, mediaType)
        } else {
            partial(path, totalLength, mediaType, range)
        }
    }

    private fun okFull(
        path: java.nio.file.Path,
        totalLength: Long,
        mediaType: MediaType
    ): ResponseEntity<StreamingResponseBody> {

        val body = StreamingResponseBody { os ->
            try {
                Files.newInputStream(path).use { input -> input.copyTo(os) }
                os.flush()
            } catch (e: IOException) {
                log.debug("Client aborted full audio stream. path={}, reason={}", path, e.message)
            }
        }

        return ResponseEntity.ok()
            .contentType(mediaType)
            .contentLength(totalLength)
            .header(HttpHeaders.ACCEPT_RANGES, "bytes")
            .body(body)
    }

    private fun partial(
        path: java.nio.file.Path,
        totalLength: Long,
        mediaType: MediaType,
        range: HttpRange
    ): ResponseEntity<StreamingResponseBody> {

        val start = range.getRangeStart(totalLength)
        val end = range.getRangeEnd(totalLength)

        if (!isValidRange(start, end, totalLength)) {
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .header(HttpHeaders.CONTENT_RANGE, "bytes */$totalLength")
                .build()
        }

        val regionLength = (end - start + 1)

        val body = StreamingResponseBody { os ->
            try {
                Files.newInputStream(path).use { raw ->
                    BufferedInputStream(raw).use { input ->
                        skipFully(input, start)

                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var remaining = regionLength

                        while (remaining > 0) {
                            val toRead = minOf(buffer.size.toLong(), remaining).toInt()
                            val read = input.read(buffer, 0, toRead)
                            if (read == -1) break
                            os.write(buffer, 0, read)
                            remaining -= read.toLong()
                        }
                        os.flush()
                    }
                }
            } catch (e: IOException) {
                log.debug(
                    "Client aborted partial audio stream. path={}, start={}, end={}, reason={}",
                    path, start, end, e.message
                )
            }
        }

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
            .contentType(mediaType)
            .contentLength(regionLength)
            .header(HttpHeaders.ACCEPT_RANGES, "bytes")
            .header(HttpHeaders.CONTENT_RANGE, "bytes $start-$end/$totalLength")
            .body(body)
    }

    private fun parseMediaTypeOrOctet(contentType: String): MediaType =
        runCatching { MediaType.parseMediaType(contentType) }
            .getOrElse { MediaType.APPLICATION_OCTET_STREAM }

    private fun isValidRange(start: Long, end: Long, totalLength: Long): Boolean {
        if (totalLength <= 0) return false
        if (start < 0) return false
        if (start >= totalLength) return false
        if (end < start) return false
        if (end >= totalLength) return false
        return true
    }

    private fun skipFully(input: BufferedInputStream, bytesToSkip: Long) {
        var remaining = bytesToSkip
        while (remaining > 0) {
            val skipped = input.skip(remaining)
            if (skipped <= 0) {
                val b = input.read()
                if (b == -1) break
                remaining -= 1
            } else {
                remaining -= skipped
            }
        }
    }
}