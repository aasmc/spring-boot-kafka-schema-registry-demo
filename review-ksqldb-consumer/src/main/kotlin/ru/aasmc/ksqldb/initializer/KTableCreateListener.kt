package ru.aasmc.ksqldb.initializer

import io.confluent.ksql.api.client.Client
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Component
import ru.aasmc.ksqldb.config.props.KSQLDbProps

private val log = LoggerFactory.getLogger(KTableCreateListener::class.java)

@Component
class KTableCreateListener(
        private val ksqlClient: Client,
        props: KSQLDbProps
): ApplicationListener<ContextRefreshedEvent> {

    val CREATE_SQL = """
            CREATE SOURCE TABLE IF NOT EXISTS ${props.reviewTable} (
                EVENTID STRING PRIMARY KEY,
                REVIEWID STRING,
                BODY STRING,
                RATING INT
            ) WITH (
                kafka_topic='${props.reviewPlacedTopic}',
                value_format='AVRO'
            );
        """
    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        try {
            val result = ksqlClient.executeStatement(CREATE_SQL).get()
            log.info("Result of creating reviews_placed_view table: {}", result.queryId().orElse(null))
        } catch (e: Exception) {
            log.error("Error: {}", e.message)
        }
    }


}