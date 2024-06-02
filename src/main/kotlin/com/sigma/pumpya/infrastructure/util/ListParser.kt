package com.sigma.pumpya.infrastructure.util

import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import reactor.core.publisher.Sinks.Empty

@Service
class ListParser {
    val adjList : List<String>  by lazy {
        val adjFile = ClassPathResource("static/adjectives.txt")
        adjFile.inputStream.bufferedReader().readLines()
    }
    val nameList : List<String> by lazy {
        val adjFile = ClassPathResource("static/animals.txt")
        adjFile.inputStream.bufferedReader().readLines()
    }

//    fun getAdjList(): List<String> {
//        val adjFile = ClassPathResource("static/adjectives.txt")
//        val result : List<String> = adjFile.inputStream.bufferedReader().readLines()
//        println(result)
//        if(result.isEmpty()) {return emptyList()
//        } else return result
//    }
//
//    fun getAnimals(): List<String> {
//        val adjFile = ClassPathResource("static/animals.txt")
//        val result : List<String> = adjFile.inputStream.bufferedReader().readLines()
//        println(result)
//        if(result.isEmpty()) {return emptyList()
//        } else return result
//    }

    fun randomNameCreator() : String {
        if (adjList.isEmpty() || nameList.isEmpty()) { return "" }
        val adj = adjList.random()
        val name = nameList.random()
        return "$adj $name"
    }

    fun getRandomName() {

    }

}