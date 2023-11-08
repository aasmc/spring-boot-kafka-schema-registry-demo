package ru.aasmc.ksqldb

import org.springframework.boot.fromApplication
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.with

@TestConfiguration(proxyBeanMethods = false)
class TestReviewKsqldbConsumerApplication

fun main(args: Array<String>) {
	fromApplication<ReviewKsqldbConsumerApplication>().with(TestReviewKsqldbConsumerApplication::class).run(*args)
}
