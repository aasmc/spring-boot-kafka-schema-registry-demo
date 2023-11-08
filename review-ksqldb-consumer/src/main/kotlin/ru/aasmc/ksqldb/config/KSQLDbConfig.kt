package ru.aasmc.ksqldb.config

import io.confluent.ksql.api.client.Client
import io.confluent.ksql.api.client.ClientOptions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.aasmc.ksqldb.config.props.KSQLDbProps

@Configuration
class KSQLDbConfig(
        private val props: KSQLDbProps
) {

    @Bean
    fun ksqlClient(): Client {
        val options = ClientOptions.create()
                .setHost(props.host)
                .setPort(props.port)
        return Client.create(options)
    }

}
