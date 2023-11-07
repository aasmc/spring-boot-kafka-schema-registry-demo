package ru.aasmc.reviewproducer.mapper

import org.springframework.stereotype.Component
import ru.aasmc.reviewproducer.dto.ReviewRequestDto
import ru.aasmc.reviewproducer.dto.ReviewResponseDto
import ru.aasmc.reviewproducer.model.ProductReview

@Component
class ReviewMapper {

    fun mapToDomain(dto: ReviewRequestDto): ProductReview =
        ProductReview(body = dto.body, rating = dto.rating)


    fun mapToDto(domain: ProductReview): ReviewResponseDto =
        ReviewResponseDto(domain.reviewId)
}