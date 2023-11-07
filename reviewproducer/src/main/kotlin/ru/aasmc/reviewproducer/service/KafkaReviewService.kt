package ru.aasmc.reviewproducer.service

import ru.aasmc.reviewproducer.model.ProductReview
import java.util.UUID

interface KafkaReviewService {

    fun sendReviewPlacedEvent(review: ProductReview, userId: UUID)

}