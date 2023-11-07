package ru.aasmc.reviewproducer.dto

data class ReviewRequestDto(
    val rating: Int,
    val body: String? = null
)
