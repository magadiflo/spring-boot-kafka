package com.magadiflo.kafka.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

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
