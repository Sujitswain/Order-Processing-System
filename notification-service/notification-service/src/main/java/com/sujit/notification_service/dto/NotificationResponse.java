package com.sujit.notification_service.dto;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Builder
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponse {

    private UUID orderId;
    private String recipient;
    private String type;
    private String status;
    private Instant sentAt;

}
