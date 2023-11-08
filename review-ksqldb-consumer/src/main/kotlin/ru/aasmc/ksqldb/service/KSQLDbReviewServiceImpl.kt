package ru.aasmc.ksqldb.service

import io.confluent.ksql.api.client.Client
import io.confluent.ksql.api.client.Row
import io.confluent.ksql.api.client.StreamedQueryResult
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.aasmc.ksqldb.config.props.KSQLDbProps
import ru.aasmc.ksqldb.dto.ReviewResponseDto
import java.util.UUID

private val log = LoggerFactory.getLogger(KSQLDbReviewServiceImpl::class.java)
@Service
class KSQLDbReviewServiceImpl(
        private val client: Client,
        private val props: KSQLDbProps
) : KSQLDbReviewService {
    override fun getAllReviews(): List<ReviewResponseDto> {
        log.info("Executing getAllReviews stream query")
        val reviews = client.streamQuery("SELECT * FROM ${props.reviewTable} LIMIT 10;")
                .get()
        return handleReviewsStreamQueryResult(reviews)
    }

    override fun getAllReviewsWithRatingAbove(threshold: Int): List<ReviewResponseDto> {
        log.info("Executing getAllReviewsWithRatingAbove: {} query", threshold)
        val reviews = client
                .streamQuery(
                        "SELECT * FROM ${props.reviewTable} AS t WHERE t.rating >= $threshold LIMIT 10;"
                )
                .get()

        return handleReviewsStreamQueryResult(reviews)
    }

    private fun handleReviewsStreamQueryResult(reviews: StreamedQueryResult): List<ReviewResponseDto> {
        val result = mutableListOf<ReviewResponseDto>()
        var row = reviews.poll()
        while (row != null) {
            result.add(mapRowToResponse(row))
            row = reviews.poll()
        }
        return result
    }
    private fun mapRowToResponse(row: Row): ReviewResponseDto {
        log.info("Mapping Stream Query Result row: {}", row)
        val reviewId = UUID.fromString(row.getString("REVIEWID"))
        val body = row.getString("BODY")
        val rating = row.getInteger("RATING")
        return ReviewResponseDto(reviewId, body, rating)
    }
}