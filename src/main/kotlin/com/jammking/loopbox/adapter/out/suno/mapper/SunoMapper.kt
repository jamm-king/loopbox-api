package com.jammking.loopbox.adapter.out.suno.mapper

import com.jammking.loopbox.adapter.`in`.web.dto.suno.prompt.SunoMusicPromptSpec
import com.jammking.loopbox.domain.entity.music.MusicConfig

object SunoMapper {

    fun MusicConfig.toPrompt(title: String): String {
        val parts = mutableListOf<String>()

        parts += "Simple loop background music"
        parts += "title of \"$title\""

        mood?.let { parts += "$it mood"}
        bpm?.let { parts += "$it bpm" }
        melody?.let { parts += "$it melody" }
        harmony?.let { parts += "$it harmony" }
        bass?.let { parts += "$it bass" }
        beat?.let { parts += "$it beat" }

        return parts.joinToString(", ")
    }

    fun SunoMusicPromptSpec.toPrompt(): String {
        val parts = mutableListOf<String>()

        parts += "Simple loop background music"
        parts += "title of \"$title\""

        mood?.let { parts += "$it mood"}
        bpm?.let { parts += "$it bpm" }
        melody?.let { parts += "$it melody" }
        harmony?.let { parts += "$it harmony" }
        bass?.let { parts += "$it bass" }
        beat?.let { parts += "$it beat" }

        return parts.joinToString(", ")
    }

    fun SunoMusicPromptSpec.toMusicConfig() =
        MusicConfig(
            mood = mood,
            bpm = bpm,
            melody = melody,
            harmony = harmony,
            bass = bass,
            beat = beat
        )

    fun String.toMusicPromptSpec(): SunoMusicPromptSpec? {
        val raw = this.trim()
        if(raw.isEmpty()) return null

        val parts = raw.split(", ")
            .map { it.trim() }
            .filter{ it.isNotEmpty() }

        var title: String? = null
        var mood: String? = null
        var bpm: Int? = null
        var melody: String? = null
        var harmony: String? = null
        var bass: String? = null
        var beat: String? = null

        val titleRegex = Regex("""title of\s+"(.+)"""", RegexOption.IGNORE_CASE)
        val moodRegex = Regex("""(.+)\s+mood$""", RegexOption.IGNORE_CASE)
        val bpmRegex = Regex("""(\d+)\s*bpm$""", RegexOption.IGNORE_CASE)
        val melodyRegex = Regex("""(.+)\s+melody$""", RegexOption.IGNORE_CASE)
        val harmonyRegex = Regex("""(.+)\s+harmony$""", RegexOption.IGNORE_CASE)
        val bassRegex = Regex("""(.+)\s+bass$""", RegexOption.IGNORE_CASE)
        val beatRegex = Regex("""(.+)\s+beat$""", RegexOption.IGNORE_CASE)

        for (part in parts) {
            when {
                part.startsWith("title of", ignoreCase = true) -> {
                    val match = titleRegex.find(part)
                    if (match != null) {
                        title = match.groupValues[1]
                    }
                }

                mood == null && moodRegex.matches(part) -> {
                    mood = moodRegex.find(part)!!.groupValues[1]
                }

                bpm == null && bpmRegex.matches(part) -> {
                    bpm = bpmRegex.find(part)!!.groupValues[1].toIntOrNull()
                }

                melody == null && melodyRegex.matches(part) -> {
                    melody = melodyRegex.find(part)!!.groupValues[1]
                }

                harmony == null && harmonyRegex.matches(part) -> {
                    harmony = harmonyRegex.find(part)!!.groupValues[1]
                }

                bass == null && bassRegex.matches(part) -> {
                    bass = bassRegex.find(part)!!.groupValues[1]
                }

                beat == null && beatRegex.matches(part) -> {
                    beat = beatRegex.find(part)!!.groupValues[1]
                }

                else -> {
                    // do nothing
                }
            }
        }

        val finalTitle = title ?: return null

        return SunoMusicPromptSpec(
            title = finalTitle,
            mood = mood,
            bpm = bpm,
            melody = melody,
            harmony = harmony,
            bass = bass,
            beat = beat
        )
    }

    fun String.toMusicConfig() =
        toMusicPromptSpec()?.toMusicConfig()
}