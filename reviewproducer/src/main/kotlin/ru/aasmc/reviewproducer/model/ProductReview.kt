package ru.aasmc.reviewproducer.model

import java.util.UUID

open class ProductReview @JvmOverloads constructor (
    var reviewId: UUID = UUID.randomUUID(),
    var body: String? = null,
    var rating: Int = 0
)