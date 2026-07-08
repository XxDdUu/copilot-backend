package com.sky_copilot.ai_copilot.export.service;

import com.sky_copilot.ai_copilot.config.MinioProperties;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Service for managing document operations in MinIO storage
 * Handles uploading, downloading, and managing document objects
 */
@Service
public class MinioDocumentService {

    private static final Logger logger = LoggerFactory.getLogger(MinioDocumentService.class);

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioProperties minioProperties;

    /**
     * Retrieves document content from MinIO storage
     *
     * @param objectKey Object key in MinIO bucket
     * @return Document content as byte array
     * @throws RuntimeException if retrieval fails
     */
    public byte[] getDocument(String objectKey) {
        try {
            logger.debug("Fetching object from MinIO: bucket={}, objectKey={}",
                    minioProperties.getBucket(), objectKey);

            try (InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(objectKey)
                            .build())) {

                byte[] content = stream.readAllBytes();
                logger.debug("Successfully read {} bytes from MinIO", content.length);
                return content;
            }
        } catch (Exception e) {
            logger.error("Failed to retrieve document from MinIO (objectKey={}): {}",
                    objectKey, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve document from MinIO: " + e.getMessage(), e);
        }
    }

    /**
     * Uploads document content to MinIO storage
     *
     * @param objectKey Object key in MinIO bucket
     * @param content Document content as byte array
     * @param contentType MIME type of the document
     * @throws RuntimeException if upload fails
     */
    public void uploadDocument(String objectKey, byte[] content, String contentType) {
        try {
            logger.debug("Uploading object to MinIO: bucket={}, objectKey={}, size={} bytes",
                    minioProperties.getBucket(), objectKey, content.length);

            try (InputStream stream = new ByteArrayInputStream(content)) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(minioProperties.getBucket())
                                .object(objectKey)
                                .stream(stream, content.length, -1)
                                .contentType(contentType)
                                .build()
                );

                logger.info("Successfully uploaded object to MinIO: objectKey={}", objectKey);
            }
        } catch (Exception e) {
            logger.error("Failed to upload document to MinIO (objectKey={}): {}",
                    objectKey, e.getMessage(), e);
            throw new RuntimeException("Failed to upload document to MinIO: " + e.getMessage(), e);
        }
    }

    /**
     * Checks if an object exists in MinIO storage
     *
     * @param objectKey Object key to check
     * @return true if object exists, false otherwise
     */
    public boolean documentExists(String objectKey) {
        try {
            getDocument(objectKey);
            return true;
        } catch (Exception e) {
            logger.debug("Document does not exist: {}", objectKey);
            return false;
        }
    }

    /**
     * Gets document size in bytes
     *
     * @param objectKey Object key in MinIO bucket
     * @return Size in bytes, or -1 if document not found
     */
    public long getDocumentSize(String objectKey) {
        try {
            byte[] content = getDocument(objectKey);
            return content.length;
        } catch (Exception e) {
            logger.warn("Failed to get document size: {}", objectKey);
            return -1;
        }
    }
}
