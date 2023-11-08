package ru.aasmc.ksqldb

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class ReviewKsqldbConsumerApplication

fun main(args: Array<String>) {
	runApplication<ReviewKsqldbConsumerApplication>(*args)
}
