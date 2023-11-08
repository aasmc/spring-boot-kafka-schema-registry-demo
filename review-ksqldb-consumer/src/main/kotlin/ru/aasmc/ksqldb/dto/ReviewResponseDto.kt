package ru.aasmc.ksqldb.dto

import java.util.UUID

data class ReviewResponseDto(
        val reviewId: UUID,
        val body: String,
        val rating: Int
)
