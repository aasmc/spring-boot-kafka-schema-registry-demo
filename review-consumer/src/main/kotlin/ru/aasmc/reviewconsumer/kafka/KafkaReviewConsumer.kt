package ru.aasmc.reviewconsumer.kafka

import org.apache.avro.generic.GenericRecord
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import java.util.UUID
import java.util.function.Consumer

private val log = LoggerFactory.getLogger(KafkaReviewConsumer::class.java)
const val X_USER_ID_HEADER = "X-USER-ID"
const val X_SERVICE_ORIGIN_HEADER = "X-SERVICE-ORIGIN"
@Configuration
class KafkaReviewConsumer {

    @Bean
    fun consumeReviewPlacedEvent() = Consumer { input: Message<GenericRecord> ->
        log.info(
            "Received ReviewPlacedEvent {}. Service origin: {}. User id = {}",
            input.payload,
            input.headers[X_SERVICE_ORIGIN_HEADER],
            input.headers[X_USER_ID_HEADER]
        )
        val record = input.payload
        val reviewId = UUID.fromString(record["reviewId"].toString())
        val body = record["body"].toString()
        val rating = record["rating"] as Int
        log.info("ReviewPlacedEvent values. ReviewId = {}, Body = {}, Rating = {}", reviewId, body, rating)
    }

}