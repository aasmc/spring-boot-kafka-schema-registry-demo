package ru.aasmc.ksqldb.service

import ru.aasmc.ksqldb.dto.ReviewResponseDto

interface KSQLDbReviewService {

    fun getAllReviews(): List<ReviewResponseDto>

    fun getAllReviewsWithRatingAbove(threshold: Int): List<ReviewResponseDto>

}