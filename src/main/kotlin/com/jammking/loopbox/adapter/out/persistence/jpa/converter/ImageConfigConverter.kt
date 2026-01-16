package com.jammking.loopbox.adapter.out.persistence.jpa.converter

import com.jammking.loopbox.domain.entity.image.ImageConfig
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class ImageConfigConverter : AttributeConverter<ImageConfig?, String?> {
    override fun convertToDatabaseColumn(attribute: ImageConfig?): String? {
        return attribute?.let { JpaJsonMapper.mapper.writeValueAsString(it) }
    }

    override fun convertToEntityAttribute(dbData: String?): ImageConfig? {
        return dbData?.let { JpaJsonMapper.mapper.readValue(it, ImageConfig::class.java) }
    }
}
