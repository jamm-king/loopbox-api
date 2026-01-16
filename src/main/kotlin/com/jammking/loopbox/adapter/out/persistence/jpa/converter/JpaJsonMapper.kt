package com.jammking.loopbox.adapter.out.persistence.jpa.converter

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

object JpaJsonMapper {
    val mapper: ObjectMapper = jacksonObjectMapper()
}
