package ru.aasmc.ksqldb.config.props

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "ksqldbprops")
class KSQLDbProps @ConstructorBinding constructor(
        val host: String,
        val port: Int,
        val reviewPlacedTopic: String,
        val reviewTable: String
)