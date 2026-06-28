package com.sky_copilot.ai_copilot.document.service;

import com.sky_copilot.ai_copilot.config.MinioProperties;
import com.sky_copilot.ai_copilot.document.dto.DocumentIngestRequest;
import com.sky_copilot.ai_copilot.document.dto.DocumentUploadResponse;
import com.sky_copilot.ai_copilot.document.entity.Document;
import com.sky_copilot.ai_copilot.document.repository.DocumentRepository;
import com.sky_copilot.ai_copilot.user.entity.User;
import com.sky_copilot.ai_copilot.user.repository.UserRepository;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    public DocumentUploadResponse upload(
            MultipartFile file,
            String uploaderEmail
    ) {
        try {
            ensureBucketExists();

            String objectKey = buildObjectKey(file.getOriginalFilename());

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(objectKey)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            User uploader = userRepository.findByEmail(uploaderEmail)
                    .orElseThrow(() -> new IllegalStateException(
                            "Uploader not found: " + uploaderEmail
                    ));

            Document document = Document.builder()
                    .fileName(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .objectKey(objectKey)
                    .uploadedBy(uploader)
                    .createdAt(LocalDateTime.now())
                    .build();

            Document saved = documentRepository.save(document);

            try {
                DocumentIngestRequest ingestRequest = DocumentIngestRequest.builder()
                        .documentId(saved.getId())
                        .bucketName(minioProperties.getBucket())
                        .objectKey(saved.getObjectKey())
                        .build();

                restTemplate.postForObject(
                        "http://localhost:8000/api/ingest",
                        ingestRequest,
                        String.class
                );
            } catch (Exception e) {
                System.err.println("Failed to trigger RAG ingestion: " + e.getMessage());
            }

            return new DocumentUploadResponse(
                    saved.getId(),
                    saved.getFileName(),
                    saved.getContentType(),
                    saved.getObjectKey(),
                    uploader.getId(),
                    saved.getCreatedAt()
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload document: " + e.getMessage(), e);
        }
    }

    private void ensureBucketExists() throws Exception {
        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                        .bucket(minioProperties.getBucket())
                        .build()
        );
        if (!exists) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .build()
            );
        }
    }

    private String buildObjectKey(String originalFilename) {
        String uuid = UUID.randomUUID().toString();
        String sanitized = originalFilename != null
                ? originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_")
                : "file";
        return "documents/" + uuid + "_" + sanitized;
    }
}
