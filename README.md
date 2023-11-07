# Kafka Schema Registry With Spring Boot Demo App

## Generate Avro Schema 
Generation of Avro Schema classes is performed with gradle plugin
`id 'com.github.davidmc24.gradle.plugin.avro' version "1.3.0"`. 

To generate classes from Avro Schema run the following command:
```shell
cd reviewproducer
./gradlew clean build
```

The generated classes for reviewproducer module will be located in the folder: 
reviewproducer/build/generated-main-avro-java

