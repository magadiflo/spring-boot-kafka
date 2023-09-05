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

## Producer Config

Crearemos la clase de configuración de nuestro Producer, quien se encargará de producir los mensajes. En este caso
solo trabajaremos con `Strings`, pero fácilmente podríamos enviar todo tipo de objetos, incluso clases personalizadas:

````java

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    public Map<String, Object> producerConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return props;
    }

    /**
     * Fábrica productora que es responsable de crear instancias productoras.
     */
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(this.producerConfig());
    }

    /**
     * Necesitamos una forma de enviar mensajes. Esto es posible con Kafka Template.
     *
     * El parámetro ProducerFactory<String, String> producerFactory del método kafkaTemplate()
     * está siendo inyectado y ese objeto inyectado es el @Bean producerFactory()
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

}
````
