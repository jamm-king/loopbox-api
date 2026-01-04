package com.jammking.loopbox

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class LoopboxApplication

fun main(args: Array<String>) {
	runApplication<LoopboxApplication>(*args)
}
