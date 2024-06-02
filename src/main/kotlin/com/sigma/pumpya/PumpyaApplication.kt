package com.sigma.pumpya

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@EnableJpaAuditing
@SpringBootApplication
class PumpyaApplication

fun main(args: Array<String>) {
	runApplication<PumpyaApplication>(*args)
}
