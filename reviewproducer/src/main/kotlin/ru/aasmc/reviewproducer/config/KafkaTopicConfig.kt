package ru.aasmc.reviewproducer.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder
import ru.aasmc.reviewproducer.config.props.KafkaProps

@Configuration
class KafkaTopicConfig(
    private val props: KafkaProps
) {

    @Bean
    fun reviewTopic(): NewTopic =
        TopicBuilder
            .name(props.reviewPlacedTopic)
            .partitions(props.partitionCount)
            .build()

}