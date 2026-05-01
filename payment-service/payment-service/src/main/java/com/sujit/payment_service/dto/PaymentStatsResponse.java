package com.sujit.payment_service.dto;

import lombok.*;

@Builder
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatsResponse {

    private long totalPayments;
    private long successCount;
    private long failedCount;

}
