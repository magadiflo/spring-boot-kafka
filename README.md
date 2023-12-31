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

## Kafka Template

De manera rápida configuraremos un @Bean para poder enviar un mensaje a nuestro topic `magadiflo` utilizando el @Bean
que creamos en la sección anterior `KafkaTemplate<String, String> kafkaTemplate()` pero inyectado como una dependencia
vía parámetro del método, veamos cómo:

````java

@SpringBootApplication
public class MainApplication {

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }

    /**
     * Usamos inyección de dependencia vía parámetro del método.
     * Enviamos nuestro mensaje al Topic: magadiflo
     */
    @Bean
    public CommandLineRunner commandLineRunner(KafkaTemplate<String, String> kafkaTemplate) {
        return args -> {
            kafkaTemplate.send("magadiflo", "Hola Kafka desde Spring Boot");
        };
    }
}
````

Si hasta este punto ejecutamos la aplicación veremos que todo se sigue ejecutando correctamente y nuestro mensaje
ya se habría enviado al topic `magadiflo`. Ahora, necesitamos una forma de verlo, así que en la siguiente sección
crearemos un Consumer para eso.

## Kafka Consumer

Otra forma de poder crear un consumidor de manera rápida, para recibir los mensajes, es a través de
la `línea de comandos`, para eso nos posicionamos en la ruta de instalación de `Kafka` y ejecutamos el siguiente
comando (notar que debemos colocar el topic que creamos `magadiflo`):

````bash
.\bin\windows\kafka-console-consumer.bat --topic magadiflo --from-beginning --bootstrap-server localhost:9092
````

Finalmente, deberíamos observar en consola nuestro mensaje enviado, veamos el ejemplo:

````bash
C:\kafka_2.13-3.5.0
.\bin\windows\kafka-console-consumer.bat --topic magadiflo --from-beginning --bootstrap-server localhost:9092
Hola Kafka desde Spring Boot
````

Ahora modifiquemos el envío de mensajes para enviar 1 millón de mensajes, **al hacerlo veremos en la consola del
consumidor cómo es que se va recepcionando los mensajes:**

````java

@SpringBootApplication
public class MainApplication {
    /* omitted code */
    @Bean
    public CommandLineRunner commandLineRunner(KafkaTemplate<String, String> kafkaTemplate) {
        return args -> {
            for (int i = 0; i < 1000_000; i++) {
                kafkaTemplate.send("magadiflo", i + ", hola!");
            }
        };
    }
}
````

## Consumer Config

Crearemos en este mismo proyecto la clase de configuración que hará de este proyecto un consumidor:

````java

@Configuration
public class KafkaConsumerConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    public Map<String, Object> consumerConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return props;
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(this.consumerConfig());
    }

    /**
     * Escucha y recibe todos los mensajes de todos los Topics o
     * particiones en un solo hilo
     */
    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> factory(ConsumerFactory<String, String> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }
}
````

## Kafka Listener

Crearemos un Listener que estará recibiendo los mensajes del productor:

````java

@Component
public class KafkaListeners {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaListeners.class);

    /**
     * El topics "magadiflo" es el que creamos con el producer.
     * En la anotación @KafkaListener también debemos pasarle un identificador de grupos
     * (groupId) de modo que si escalamos, es decir, si tenemos más instancias de la
     * misma aplicación, básicamente pueden leer desde la misma partición o topic, así que
     * le asignaremos un identificador, por ejemplo "magadifloId" (tiene que ser un
     * identificador único).
     */
    @KafkaListener(topics = {"magadiflo"}, groupId = "magadifloId")
    public void listener(String data) {
        LOG.info("Dato recibido: {}", data);
    }
}
````

Hasta este punto ejecutamos la aplicación y observamos en consola del IDE IntelliJ IDEA parte del resultado obtenido:

````bash
INFO 10204 --- [ad | producer-1] o.a.k.c.p.internals.TransactionManager   : [Producer clientId=producer-1] ProducerId set to 0 with epoch 0
INFO 10204 --- [ntainer#0-0-C-1] o.s.k.l.KafkaMessageListenerContainer    : magadifloId: partitions assigned: [magadiflo-0]
INFO 10204 --- [ntainer#0-0-C-1] com.magadiflo.kafka.app.KafkaListeners   : Dato recibido: 0, hola!
INFO 10204 --- [ntainer#0-0-C-1] com.magadiflo.kafka.app.KafkaListeners   : Dato recibido: 1, hola!
INFO 10204 --- [ntainer#0-0-C-1] com.magadiflo.kafka.app.KafkaListeners   : Dato recibido: 2, hola!
INFO 10204 --- [ntainer#0-0-C-1] com.magadiflo.kafka.app.KafkaListeners   : Dato recibido: 3, hola!
INFO 10204 --- [ntainer#0-0-C-1] com.magadiflo.kafka.app.KafkaListeners   : Dato recibido: 4, hola!
INFO 10204 --- [ntainer#0-0-C-1] com.magadiflo.kafka.app.KafkaListeners   : Dato recibido: 5, hola!
INFO 10204 --- [ntainer#0-0-C-1] com.magadiflo.kafka.app.KafkaListeners   : Dato recibido: 6, hola!
INFO 10204 --- [ntainer#0-0-C-1] com.magadiflo.kafka.app.KafkaListeners   : Dato recibido: 7, hola!
INFO 10204 --- [ntainer#0-0-C-1] com.magadiflo.kafka.app.KafkaListeners   : Dato recibido: 8, hola!
INFO 10204 --- [ntainer#0-0-C-1] com.magadiflo.kafka.app.KafkaListeners   : Dato recibido: 9, hola!
````

Recordemos que también tenemos un consumidor en la línea de comandos:

````bash
C:\kafka_2.13-3.5.0
.\bin\windows\kafka-console-consumer.bat --topic magadiflo --from-beginning --bootstrap-server localhost:9092
0, hola!
1, hola!
2, hola!
3, hola!
4, hola!
5, hola!
6, hola!
7, hola!
8, hola!
9, hola!
````

Bien, todo está funcionando correctamente. Hasta este punto tenemos dos consumidores, uno que tenemos en
la `línea de comandos` y el otro, nuestra propia aplicación de `Spring Boot`.

## Restful API y Kafka

Crearemos un Record para poder recibir el mensaje desde el cliente:

````java
public record MessageRequest(String message) {
}
````

Ahora, creamos nuestro controlador que recibirá los mensajes enviados desde el cliente HTTP. Aquí utilizamos
el `KafkaTemplate` para poder producir los mensajes:

````java

@RestController
@RequestMapping(path = "/api/v1/messages")
public class MessageController {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public MessageController(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping
    public void publish(@RequestBody MessageRequest request) {
        this.kafkaTemplate.send("magadiflo", request.message());
    }
}
````

Finalmente, ejecutamos la aplicación y enviamos un mensaje vía http con nuestro cliente curl:

````bash
curl -v -X POST -H "Content-Type: application/json" -d "{\"message\": \"Api Rest con Kafka\"}" http://localhost:8080/api/v1/messages | jq

< HTTP/1.1 200
````

Como resultado obtenemos en consola del IDE:

````bash
2023-09-05T20:11:47.925-05:00  INFO 10596 --- [ntainer#0-0-C-1] com.magadiflo.kafka.app.KafkaListeners   : Dato recibido: Api Rest con Kafka
````

Lo mismo ocurre en la línea de comando:

````bash
C:\kafka_2.13-3.5.0
.\bin\windows\kafka-console-consumer.bat --topic magadiflo --from-beginning --bootstrap-server localhost:9092
Api Rest con Kafka
````