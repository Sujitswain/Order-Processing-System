package com.sujit.pdf_service.config;

import com.sujit.pdf_service.event.OrderSuccessEvent;
import com.sujit.pdf_service.event.PdfGeneratedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public KafkaTemplate<String, PdfGeneratedEvent> pdfGeneratedKafkaTemplate() {
        return new KafkaTemplate<>(pdfGeneratedProducerFactory());
    }

    @Bean
    public ProducerFactory<String, PdfGeneratedEvent> pdfGeneratedProducerFactory() {
        Map<String, Object> props = new HashMap<>();

        // bootstrap-server config
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // key serializer
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // value serializer
        JacksonJsonSerializer<PdfGeneratedEvent> serializer = new JacksonJsonSerializer<>();
        serializer.setAddTypeInfo(true);

        return new DefaultKafkaProducerFactory<>(
                props,
                new StringSerializer(),
                serializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderSuccessEvent> orderSuccessKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OrderSuccessEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(orderSuccessConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, OrderSuccessEvent> orderSuccessConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "pdf-service-group");

        JacksonJsonDeserializer<OrderSuccessEvent> deserializer = new JacksonJsonDeserializer<>();
        deserializer.addTrustedPackages("com.sujit.*");

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new ErrorHandlingDeserializer<>(deserializer)
        );
    }

}