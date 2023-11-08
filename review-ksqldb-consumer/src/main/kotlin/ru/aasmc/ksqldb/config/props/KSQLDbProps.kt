package ru.aasmc.ksqldb.config.props

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "ksqldbprops")
class KSQLDbProps @ConstructorBinding constructor(
        var host: String,
        var port: Int
)