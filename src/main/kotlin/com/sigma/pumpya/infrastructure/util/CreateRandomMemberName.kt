package com.sigma.pumpya.infrastructure.util

import java.io.File


class CreateRandomMemberName {
    private fun getAdjList(): List<String> {
        val inputStream = javaClass.classLoader.getResourceAsStream("adjective.txt")
        return inputStream?.bufferedReader()?.readLines() ?: emptyList()
    }

    private fun getAnimals(): List<String> {
        val inputStream = javaClass.classLoader.getResourceAsStream("animals.txt")
        return inputStream?.bufferedReader()?.readLines() ?: emptyList()
    }

    fun getRandomName() {

    }
}