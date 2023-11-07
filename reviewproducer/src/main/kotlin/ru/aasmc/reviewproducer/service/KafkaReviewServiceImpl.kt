package ru.aasmc.reviewproducer.service

import org.slf4j.LoggerFactory
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import ru.aasmc.review.ReviewPlacedEvent
import ru.aasmc.reviewproducer.model.ProductReview
import ru.aasmc.reviewproducer.util.ServiceUtil
import java.util.*

private val log = LoggerFactory.getLogger(KafkaReviewServiceImpl::class.java)
const val X_USER_ID_HEADER = "X-USER-ID"
const val REVIEW_PLACED_BINDING = "reviewSupplier-out-0"
const val X_SERVICE_ORIGIN_HEADER = "X-SERVICE-ORIGIN"
const val X_PARTITION_KEY_HEADER = "X-PARTITION-KEY"
@Service
class KafkaReviewServiceImpl(
    private val streamBridge: StreamBridge,
    private val serviceUtil: ServiceUtil
) : KafkaReviewService {
    override fun sendReviewPlacedEvent(review: ProductReview, userId: UUID) {
        val event = ReviewPlacedEvent.newBuilder()
            .setReviewId(review.reviewId.toString())
            .setBody(review.body)
            .setRating(review.rating)
            .setEventId(UUID.randomUUID().toString())
            .build()
        sendMessage(REVIEW_PLACED_BINDING, event, userId)
    }

    private fun sendMessage(bindingName: String, event: ReviewPlacedEvent, userId: UUID) {
        log.debug("Sending a {} message to {}", event, bindingName)
        val message = MessageBuilder.withPayload(event)
            .setHeader(X_PARTITION_KEY_HEADER, event.reviewId)
            .setHeader(X_USER_ID_HEADER, userId)
            .setHeader(X_SERVICE_ORIGIN_HEADER, serviceUtil.getServiceAddress())
            .build()
        streamBridge.send(bindingName, message)
    }
}