package com.sujit.order_service.service;

import com.sujit.order_service.config.CorrelationIdFilter;
import com.sujit.order_service.config.CorrelationIdHolder;
import java.nio.charset.StandardCharsets;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(String topic, Object payload) {
        ProducerRecord<String, Object> record = new ProducerRecord<>(topic, payload);
        String correlationId = CorrelationIdHolder.getCorrelationId();

        // add the ID into payload, so other microservices can trace this request.
        if (correlationId != null) {
            record.headers().add(CorrelationIdFilter.CORRELATION_HEADER, correlationId.getBytes(StandardCharsets.UTF_8));
        }
        kafkaTemplate.send(record);
    }
}
