package ru.aasmc.reviewproducer

import org.springframework.boot.fromApplication
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.with

@TestConfiguration(proxyBeanMethods = false)
class TestReviewproducerApplication

fun main(args: Array<String>) {
    fromApplication<ReviewproducerApplication>().with(TestReviewproducerApplication::class).run(*args)
}
