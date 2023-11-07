package ru.aasmc.reviewconsumer

import org.springframework.boot.fromApplication
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.with

@TestConfiguration(proxyBeanMethods = false)
class TestReviewConsumerApplication

fun main(args: Array<String>) {
    fromApplication<ReviewConsumerApplication>().with(TestReviewConsumerApplication::class).run(*args)
}
