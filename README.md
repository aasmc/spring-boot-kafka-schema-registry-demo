# Kafka Schema Registry With Spring Boot Demo App

Demo app that demonstrates the use of the following technologies: 
- Apache Kafka
- Avro serialization 
- Confluent Schema Registry
- kSQLDb

The project contains the following subprojects, implemented as independent spring-boot services:
- Review Producer (here all the Review Events originate)
- Review Consumer (simple Spring Cloud Stream Kafka consumer)
- Review KSQLDb Consumer (consumer that uses KSQLDb to capture ReviewPlacedEvents from Kafka topic and store them in KSQLDb)

Logic of the app is very simple:
A client can POST a review to a Review Producer endpoint. Review Producer then handles the review and sends
ReviewPlacedEvent to a Kafka topic. 
A client can also ask ReviewKsqldbConsumer for all reviews (actually it returns 10) or reviews with rating above specified threshold.

## Review Producer
Exposes HTTP endpoint:
- URL: http://localhost:9000/reviews/v1
- Method: POST
- RequestBody (example): 
```json
{
    "rating": 5,
    "body": "Excellent Product"
}
```
- Headers: X-USER-ID  UUID

After receiving the POST request, Review Producer sends ReviewPlacedEvent to Kafka via
Spring Cloud Stream Kafka Binder. Below is the application.yml of Review Producer.

```yml
spring:
  application:
    name: review-producer
  cloud:
    stream:
      bindings:
        reviewSupplier-out-0:
          destination: ${kafkaprops.reviewPlacedTopic}
          producer:
            use-native-encoding: true
            partition-key-expression: headers['X-PARTITION-KEY']
            partition-count: 3
      kafka:
        binder:
          brokers: localhost:9092
          replication-factor: 1
        bindings:
          reviewSupplier-out-0:
            producer:
              configuration:
                value.serializer: io.confluent.kafka.serializers.KafkaAvroSerializer
                schema.registry.url: http://localhost:8081


kafkaprops:
  reviewPlacedTopic: review.placement.v1
  partitionCount: 3
server:
  port: 9000
```

`schema.registry.url` property specifies the location of Confluent Schema Registry, that handles message schemas in Avro format.

Partition-key-expression is used to send messages to different partitions of the Kafka topic: review.placement.v1
It is used in `KafkaReviewServiceImpl` class when sending message via `StreamBridge`:
```kotlin
    private fun sendMessage(bindingName: String, event: ReviewPlacedEvent, userId: UUID) {
        log.debug("Sending a {} message to {}", event, bindingName)
        val message = MessageBuilder.withPayload(event)
            .setHeader(X_PARTITION_KEY_HEADER, event.reviewId)
            .setHeader(X_USER_ID_HEADER, userId)
            .setHeader(X_SERVICE_ORIGIN_HEADER, serviceUtil.getServiceAddress())
            .build()
        streamBridge.send(bindingName, message)
    }
```

Class ReviewPlacedEvent is auto-generated by Avro generator gradle plugin: `id 'com.github.davidmc24.gradle.plugin.avro' version "1.3.0"`.
Avro schema is specified in src/main/avro directory:
```json
{
  "type": "record",
  "name": "ReviewPlacedEvent",
  "namespace": "ru.aasmc.review",
  "fields": [
    {
      "name": "eventId",
      "type": {
        "type": "string",
        "logicalType": "uuid"
      }
    },
    {
      "name": "reviewId",
      "type": {
        "type": "string",
        "logicalType": "uuid"
      },
      "doc": "Unique identifier of the event in the form of UUID"
    },
    {
      "name": "body",
      "type": "string"
    },
    {
      "name": "rating",
      "type": "int"
    }
  ]
}
```

To generate classes from Avro Schema run the following command:
```shell
cd reviewproducer
./gradlew clean build
```

The generated classes for reviewproducer module are located in the folder: 
reviewproducer/build/generated-main-avro-java

Complete build.gradle of the Review Producer:

```groovy
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        gradlePluginPortal()
        maven {
            url = "https://packages.confluent.io/maven/"
        }
        maven {
            url = "https://jitpack.io"
        }
    }
}

plugins {
    id 'org.springframework.boot' version '3.1.5'
    id 'io.spring.dependency-management' version '1.1.3'
    id 'org.jetbrains.kotlin.jvm' version '1.8.22'
    id 'org.jetbrains.kotlin.plugin.spring' version '1.8.22'
    id "com.github.imflog.kafka-schema-registry-gradle-plugin" version "1.6.0"
    id 'com.github.davidmc24.gradle.plugin.avro' version "1.3.0"
}

group = 'ru.aasmc'
version = '0.0.1-SNAPSHOT'

java {
    sourceCompatibility = '17'
}

repositories {
    mavenCentral()
    maven {
        url = "https://packages.confluent.io/maven/"
    }
    maven {
        url = "https://jitpack.io"
    }
}

ext {
    set('springCloudVersion', "2022.0.4")
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'com.fasterxml.jackson.module:jackson-module-kotlin'
    implementation 'org.jetbrains.kotlin:kotlin-reflect'
    implementation 'org.springframework.cloud:spring-cloud-starter-stream-kafka'
    implementation("io.confluent:kafka-avro-serializer:5.3.0")
    implementation("org.apache.avro:avro:1.11.0")
    annotationProcessor 'org.springframework.boot:spring-boot-configuration-processor'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.boot:spring-boot-testcontainers'
    testImplementation 'org.springframework.cloud:spring-cloud-stream-test-binder'
    testImplementation 'org.testcontainers:junit-jupiter'
    testImplementation 'org.testcontainers:kafka'
}

dependencyManagement {
    imports {
        mavenBom "org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}"
    }
}

schemaRegistry {
    url = 'http://localhost:8081'
    quiet = true
    register {
        subject('avro-reviewplaced-value', 'src/main/avro/review-placed.avsc', 'AVRO')
    }
}

tasks.withType(KotlinCompile) {
    kotlinOptions {
        freeCompilerArgs += '-Xjsr305=strict'
        jvmTarget = '17'
    }
}

tasks.named('test') {
    useJUnitPlatform()
}
```

## Review Consumer Service
Consumes messages from Kafka topic: review.placement.v1 via Spring Cloud Stream Kafka Binder.
Below is the Review Consumer application.yml

```yml
server:
  port: 9001

spring:
  application:
    name: review-consumer
  cloud:
    stream:
      bindings:
        consumeReviewPlacedEvent-in-0:
          destination: ${kafkaprops.reviewPlacedTopic}
          group: ${spring.application.name}
          consumer:
            use-native-decoding: true
      kafka:
        binder:
          brokers: localhost:9092
        bindings:
          consumeReviewPlacedEvent-in-0:
            consumer:
              configuration:
                value.deserializer: io.confluent.kafka.serializers.KafkaAvroDeserializer
                schema.registry.url: http://localhost:8081
                specific.avro.reader: true


kafkaprops:
  reviewPlacedTopic: review.placement.v1
```

`spring.cloud.stream.bindings.consumeReviewPlacedEvent-in-0.group` property is used to organize
all instances of Review Consumer Service into a single consumer group. The topic partitions will be
distributed among the Review Consumer instances.

`specific.avro.reader` property enables consumer to read specific message (ReviewPlacedEvent) and not the generic message.

Complete build.gradle of the Review Consumer:
```groovy
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        gradlePluginPortal()
        maven {
            url = "https://packages.confluent.io/maven/"
        }
        maven {
            url = "https://jitpack.io"
        }
    }
}

plugins {
    id 'org.springframework.boot' version '3.1.5'
    id 'io.spring.dependency-management' version '1.1.3'
    id 'org.jetbrains.kotlin.jvm' version '1.8.22'
    id 'org.jetbrains.kotlin.plugin.spring' version '1.8.22'
    id "com.github.imflog.kafka-schema-registry-gradle-plugin" version "1.6.0"
    id 'com.github.davidmc24.gradle.plugin.avro' version "1.3.0"
}

group = 'ru.aasmc'
version = '0.0.1-SNAPSHOT'

java {
    sourceCompatibility = '17'
}

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories {
    mavenCentral()
    maven {
        url = "https://packages.confluent.io/maven/"
    }
    maven {
        url = "https://jitpack.io"
    }
}

ext {
    set('springCloudVersion', "2022.0.4")
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'com.fasterxml.jackson.module:jackson-module-kotlin'
    implementation 'org.jetbrains.kotlin:kotlin-reflect'
    implementation 'org.springframework.cloud:spring-cloud-starter-stream-kafka'
    implementation("io.confluent:kafka-avro-serializer:5.3.0")
    implementation("org.apache.avro:avro:1.11.0")
    annotationProcessor 'org.springframework.boot:spring-boot-configuration-processor'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.boot:spring-boot-testcontainers'
    testImplementation 'org.springframework.cloud:spring-cloud-stream-test-binder'
    testImplementation 'org.testcontainers:junit-jupiter'
    testImplementation 'org.testcontainers:kafka'
}

dependencyManagement {
    imports {
        mavenBom "org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}"
    }
}

schemaRegistry {
    url = 'http://localhost:8081'
    quiet = true
}

tasks.withType(KotlinCompile) {
    kotlinOptions {
        freeCompilerArgs += '-Xjsr305=strict'
        jvmTarget = '17'
    }
}

tasks.named('test') {
    useJUnitPlatform()
}
```

## Review KSQLDb Consumer
Collects reviews from Kafka topic `review.placement.v1` in a KSQLDb table `reviews_placed_view`.
The table is created on start up by executing the following statement: 
```sql
CREATE SOURCE TABLE IF NOT EXISTS reviews_placed_view (
    EVENTID STRING PRIMARY KEY,
    REVIEWID STRING,
    BODY STRING,
    RATING INT
) WITH (
    kafka_topic='review.placement.v1',
    value_format='AVRO'
);
```

The table is stored on ksqldb server, which is running in docker container.
To connect to the ksqldb server we use `io.confluent.ksql.api.client.Client`:
```kotlin
@Bean
fun ksqlClient(): Client {
    val options = ClientOptions.create()
            .setHost(props.host)
            .setPort(props.port)
    return Client.create(options)
}
```

ReviewKsqldbConsumer exposes the following endpoints:
- GET http://localhost:9010/reviews/v1 to retrieve all reviews (actually 10)
- GET http://localhost:9010/reviews/v1/{int} (to retrieve all reviews with rating above or equal to path variable param)

Information is retrieved from ksqldb via configured Client. 

```kotlin
override fun getAllReviews(): List<ReviewResponseDto> {
    log.info("Executing getAllReviews stream query")
    val reviews = client.streamQuery("SELECT * FROM ${props.reviewTable} LIMIT 10;")
            .get()
    return handleReviewsStreamQueryResult(reviews)
}

private fun mapRowToResponse(row: Row): ReviewResponseDto {
    log.info("Mapping Stream Query Result row: {}", row)
    val reviewId = UUID.fromString(row.getString("REVIEWID"))
    val body = row.getString("BODY")
    val rating = row.getInteger("RATING")
    return ReviewResponseDto(reviewId, body, rating)
}
```

## Starting the application:
1. Start Docker containers with the following command from the root dir:
```shell
docker-compose up -d
```
There are 6 containers:
- Zookeper
- Kafka
- Schema Registry
- Postgres (for kadeck)
- kadeck (UI tool to monitor Kafka topics and Schema registry) available at http://localhost:80
- ksqldb-server

2. When all containers are up, register AVRO schema with the following command from reviewproducer dir:
```shell
 ./gradlew registerSchemasTask 
```
This is a Gradle Task, executed by Gradle plugin `id "com.github.imflog.kafka-schema-registry-gradle-plugin" version "1.6.0"`.
Here is the configuration of the schemaRegistry task in gradle:
```groovy
schemaRegistry {
    url = 'http://localhost:8081'
    quiet = true
    register {
        subject('avro-reviewplaced-value', 'src/main/avro/review-placed.avsc', 'AVRO')
    }
}
```
3. After successfully registering the schema, run all the Spring Boot apps from the root project dir on your local machine. 
4. Now you can send:
   - HTTP POST request to http://localhost:9000/reviews/v1
   - HTTP GET request to http://localhost:9010/reviews/v1 (retrieves 10 reviews)
   - HTTP GET request to http://localhost:9010/reviews/v1/{int} (reviews with rating above or equal to path variable)
You can use Postman to send requests. Example postman collection with single POST request is in
postman directory. 

## Useful Links 
- https://developer.confluent.io/courses/schema-registry/key-concepts/
- https://github.com/confluentinc/learn-kafka-courses
- https://github.com/rogervinas/spring-cloud-stream-kafka-confluent-avro-schema-registry
- https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka_overview.html
- https://piotrminkowski.com/2022/06/22/introduction-to-ksqldb-on-kubernetes-with-spring-boot/