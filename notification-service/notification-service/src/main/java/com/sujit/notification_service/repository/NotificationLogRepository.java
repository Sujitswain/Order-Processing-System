package com.sujit.notification_service.repository;

import com.sujit.notification_service.entity.NotificationLog;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID> {
    List<NotificationLog> findByOrderId(UUID orderId);
}
