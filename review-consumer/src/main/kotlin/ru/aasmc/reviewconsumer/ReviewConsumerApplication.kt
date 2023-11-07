package ru.aasmc.reviewconsumer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ReviewConsumerApplication

fun main(args: Array<String>) {
    runApplication<ReviewConsumerApplication>(*args)
}
