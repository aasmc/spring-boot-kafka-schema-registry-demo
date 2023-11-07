package ru.aasmc.reviewproducer.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.aasmc.reviewproducer.dto.ReviewRequestDto
import ru.aasmc.reviewproducer.dto.ReviewResponseDto
import ru.aasmc.reviewproducer.mapper.ReviewMapper
import java.util.*

private val log = LoggerFactory.getLogger(ReviewServiceImpl::class.java)
@Service
class ReviewServiceImpl(
    private val mapper: ReviewMapper,
    private val kafkaService: KafkaReviewService
) : ReviewService {
    override fun placeReview(
        dto: ReviewRequestDto,
        userId: UUID
    ): ReviewResponseDto {
        val review = mapper.mapToDomain(dto)
        log.info("Handling review {} by user with ID={}", review, userId)
        kafkaService.sendReviewPlacedEvent(review, userId)
        return mapper.mapToDto(review)
    }
}