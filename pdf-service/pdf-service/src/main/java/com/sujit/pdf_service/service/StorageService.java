package com.sujit.pdf_service.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import com.sujit.pdf_service.entity.DocumentEntity;
import com.sujit.pdf_service.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Service
public class StorageService {

    @Value("${aws.upload-to-s3}")
    private boolean uploadToS3;

    @Value("${aws.accessKeyId}")
    private String accessKeyId;

    @Value("${aws.secretAccessKey}")
    private String secretAccessKey;

    @Value("${aws.region:us-east-1}")
    private String region;

    @Value("${aws.storage.bucket}")
    private String bucketName;

    @Value("${aws.storage.base-url}")
    private String baseUrl;

    private S3Client s3Client;

    private final DocumentRepository documentRepository;

    public StorageService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @PostConstruct
    public void init() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        this.s3Client = S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .build();
    }

    public String savePdf(UUID orderId, String fileName, byte[] pdfContent) throws IOException {
        DocumentEntity document = new DocumentEntity();
        document.setOrderId(orderId);
        document.setFileName(fileName);
        document.setContentType("application/pdf");
        document.setCreatedAt(Instant.now());

        if (uploadToS3) {
            // Upload to S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType("application/pdf")
                .build();
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(pdfContent));

            String pdfUrl = "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + fileName;
            document.setStorageUrl(pdfUrl);
            document.setUploadedToS3(true);
            document.setPdfBase64(null);

            log.info("PDF uploaded to S3: {}", pdfUrl);
        } else {
            document.setStorageUrl(null);
            document.setUploadedToS3(false);
            document.setPdfBase64(Base64.getEncoder().encodeToString(pdfContent));

            log.info("PDF saved to database for order: {}", orderId);
        }

        documentRepository.save(document);

        return document.getStorageUrl();
    }

    public byte[] getPdfByOrderId(UUID orderId) throws IOException {
        DocumentEntity document = documentRepository.findFirstByOrderIdOrderByCreatedAtDesc(orderId)
                .orElseThrow(() -> new IOException("Document not found for order: " + orderId));

        if (document.getPdfBase64() != null) {
            log.info("Retrieving PDF from database for order: {}", orderId);
            return Base64.getDecoder().decode(document.getPdfBase64());
        } else if (document.getStorageUrl() != null) {
            // Download from S3
            log.info("Retrieving PDF from S3 for order: {}", orderId);
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(document.getFileName())
                .build();
            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);
            
            log.info("PDF retrieved from S3 for order: {}", orderId);
            return objectBytes.asByteArray();
        } else {
            throw new IOException("PDF content not available for order: " + orderId);
        }
    }
}
