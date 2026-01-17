package com.jammking.loopbox.adapter.out.persistence.jpa.converter

import com.jammking.loopbox.domain.entity.music.MusicConfig
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class MusicConfigConverter : AttributeConverter<MusicConfig?, String?> {
    override fun convertToDatabaseColumn(attribute: MusicConfig?): String? {
        return attribute?.let { JpaJsonMapper.mapper.writeValueAsString(it) }
    }

    override fun convertToEntityAttribute(dbData: String?): MusicConfig? {
        return dbData?.let { JpaJsonMapper.mapper.readValue(it, MusicConfig::class.java) }
    }
}
