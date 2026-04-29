package com.sujit.payment_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic orderCreatedTopic() {
        return new NewTopic("order-created", 1, (short) 1);
    }

    @Bean
    public NewTopic paymentSuccessTopic() {
        return new NewTopic("payment-success", 1, (short) 1);
    }

    @Bean
    public NewTopic paymentFailedTopic() {
        return new NewTopic("payment-failed", 1, (short) 1);
    }

    @Bean
    public NewTopic paymentFailedDltTopic() {
        return new NewTopic("payment-failed.DLT", 1, (short) 1);
    }
}
