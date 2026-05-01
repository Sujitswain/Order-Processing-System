package com.sujit.order_service.config;

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
    public NewTopic orderCompletedTopic() {
        return new NewTopic("order-completed", 1, (short) 1);
    }

    @Bean
    public NewTopic orderCancelledTopic() {
        return new NewTopic("order-cancelled", 1, (short) 1);
    }

    @Bean
    public NewTopic paymentFailedTopic() {
        return new NewTopic("payment-failed", 1, (short) 1);
    }

    @Bean
    public NewTopic paymentSuccessTopic() {
        return new NewTopic("payment-success", 1, (short) 1);
    }

    @Bean
    public NewTopic orderCompletedDltTopic() {
        return new NewTopic("order-completed.DLT", 1, (short) 1);
    }

    @Bean
    public NewTopic paymentFailedDltTopic() {
        return new NewTopic("payment-failed.DLT", 1, (short) 1);
    }
}
