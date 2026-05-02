package com.sujit.notification_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic paymentSuccessTopic() {
        return new NewTopic("payment-success", 1, (short) 1);
    }

    @Bean
    public NewTopic paymentFailedTopic() {
        return new NewTopic("payment-failed", 1, (short) 1);
    }

    @Bean
    public NewTopic orderCompletedTopic() {
        return new NewTopic("order-completed", 1, (short) 1);
    }

    @Bean
    public NewTopic notificationDltTopic() {
        return new NewTopic("notification-events.DLT", 1, (short) 1);
    }
}
