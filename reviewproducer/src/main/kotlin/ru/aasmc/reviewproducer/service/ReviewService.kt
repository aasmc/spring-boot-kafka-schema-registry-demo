package ru.aasmc.reviewproducer.service

import ru.aasmc.reviewproducer.dto.ReviewRequestDto
import ru.aasmc.reviewproducer.dto.ReviewResponseDto
import java.util.UUID

interface ReviewService {

    fun placeReview(dto: ReviewRequestDto, userId: UUID): ReviewResponseDto

}