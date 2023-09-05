# [Kafka Tutorial - Spring Boot Microservices](https://www.youtube.com/watch?v=SqVfCyfCJqw)

Tutorial tomado de **Amigoscode**

---

## Dependencias iniciales

Para construir este proyecto de Spring Boot con Apache Kafka solo necesitamos la dependencia **web y de Kafka:**

````xml
<!--Spring Boot: 3.1.3-->
<!--Java: 17-->
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
````

## Creando un topic

Primero debemos configurar la **url donde se estará ejecutando Kafka**, por defecto se ejecutará en el puerto **9092**:

````properties
spring.kafka.bootstrap-servers=localhost:9092
````

Creamos una clase de configuración cuyo @Bean retornará un `NewTopic` llamado `magadiflo`:

````java

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic generateTopic() {
        return TopicBuilder.name("magadiflo")
                .build();
    }

}
````

## Ejecutando creación del Topic

Antes de ejecutar el proyecto debemos tener levantado el servidor `Zookeeper y Kafka`. Para eso, debemos posicionarnos
mediante cmd en la ruta de Kafka y ejecutar los comandos siguientes:

````bash
.\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties
````

````bash
.\bin\windows\kafka-server-start.bat .\config\server.properties
````

**NOTA**

> Para este tutorial **ya tenía Kafka configurado**, lo hice cuando llevé el tutorial siguiente
> [spring-boot-for-apache-kafka](https://github.com/magadiflo/spring-boot-for-apache-kafka.git), así que hasta este
> punto solo nos encargamos de levantar los servidores **Zookeeper y Kafka**.

Listo, ahora **si ejecutamos la aplicación y veremos que todo se ejecuta sin errores.** Ahora, para ver si nuestro
`topic magadiflo` fue creado, podemos abrir la terminal en la ruta de nuestra instalación de kafka y ejecutar el
siguiente comando `.\bin\windows\kafka-topics.bat --list --bootstrap-server localhost:9092`, ejemplo:

````bash
C:\kafka_2.13-3.5.0
.\bin\windows\kafka-topics.bat --list --bootstrap-server localhost:9092
__consumer_offsets
example-topic
magadiflo
magadiflo-topic
````
