package com.sigma.pumpya

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PumpyaApplication

fun main(args: Array<String>) {
	runApplication<PumpyaApplication>(*args)
}
