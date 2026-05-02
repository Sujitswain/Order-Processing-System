package com.sujit.inventory_service.service;

import com.sujit.inventory_service.config.CorrelationIdFilter;
import com.sujit.inventory_service.config.CorrelationIdHolder;
import java.nio.charset.StandardCharsets;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class InventoryProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InventoryProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(String topic, Object payload) {
        ProducerRecord<String, Object> record = new ProducerRecord<>(topic, payload);
        String correlationId = CorrelationIdHolder.getCorrelationId();
        if (correlationId != null) {
            record.headers().add(CorrelationIdFilter.CORRELATION_HEADER, correlationId.getBytes(StandardCharsets.UTF_8));
        }
        kafkaTemplate.send(record);
    }
}
