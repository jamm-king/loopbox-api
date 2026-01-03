package com.jammking.loopbox.domain.entity.file

import com.jammking.loopbox.domain.exception.file.InvalidAudioFilePathException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class AudioFileTest {

    @Test
    fun `constructor should throw exception when path is blank`() {
        assertThrows(InvalidAudioFilePathException::class.java) {
            AudioFile(path = "")
        }
    }

    @Test
    fun `constructor should create instance when path is valid`() {
        val path = "/path/to/file.mp3"
        val audioFile = AudioFile(path = path)

        assertEquals(path, audioFile.path)
    }
}
