package ru.aasmc.reviewproducer.config.props

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "kafkaprops")
class KafkaProps @ConstructorBinding constructor(
    var reviewPlacedTopic: String,
    var partitionCount: Int
)