package com.sujit.pdf_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Builder
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "documents")
public class DocumentEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "storage_url")
    private String storageUrl;

    @Lob
    @Column(name = "pdf_base64", columnDefinition = "CLOB")
    private String pdfBase64;

    @Column(name = "uploaded_to_s3", nullable = false)
    private boolean uploadedToS3;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

}
