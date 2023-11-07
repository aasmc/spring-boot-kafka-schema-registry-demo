package ru.aasmc.reviewproducer.controller

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.aasmc.reviewproducer.dto.ReviewRequestDto
import ru.aasmc.reviewproducer.dto.ReviewResponseDto
import ru.aasmc.reviewproducer.service.ReviewService
import java.util.UUID

private val log = LoggerFactory.getLogger(ReviewController::class.java)
@RestController
@RequestMapping("/reviews/v1")
class ReviewController(
    private val reviewService: ReviewService
) {

    @PostMapping
    fun createReview(
        @RequestBody dto: ReviewRequestDto,
        @RequestHeader("X-USER-ID") userId: UUID
    ): ReviewResponseDto {
        log.info("Received POST request to create review {}", dto)
        return reviewService.placeReview(dto, userId)
    }
}