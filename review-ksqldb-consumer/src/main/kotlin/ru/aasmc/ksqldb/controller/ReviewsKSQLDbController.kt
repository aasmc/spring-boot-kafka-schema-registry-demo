package ru.aasmc.ksqldb.controller

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.aasmc.ksqldb.dto.ReviewResponseDto
import ru.aasmc.ksqldb.service.KSQLDbReviewService

private val log = LoggerFactory.getLogger(ReviewsKSQLDbController::class.java)

@RestController
@RequestMapping("/reviews/v1")
class ReviewsKSQLDbController(
        private val service: KSQLDbReviewService
) {

    @GetMapping
    fun getAllReviews(): List<ReviewResponseDto> {
        log.info("Received request to GET all reviews")
        return service.getAllReviews()
    }

    @GetMapping("/{rating}")
    fun getReviewsWithRating(@PathVariable("rating") rating: Int): List<ReviewResponseDto> {
        log.info("Received request to GET all reviews with rating above: {}", rating)
        return service.getAllReviewsWithRatingAbove(rating)
    }

}