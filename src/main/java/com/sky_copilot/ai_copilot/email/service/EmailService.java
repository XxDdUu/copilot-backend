package com.sky_copilot.ai_copilot.email.service;

import com.sky_copilot.ai_copilot.email.dto.SendEmailRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import com.sky_copilot.ai_copilot.document.entity.Document;
import com.sky_copilot.ai_copilot.document.repository.DocumentRepository;
import io.minio.MinioClient;
import io.minio.GetObjectArgs;
import com.sky_copilot.ai_copilot.config.MinioProperties;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final DocumentRepository documentRepository;
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    public void send(SendEmailRequest request) throws Exception {

        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper =
                new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

        helper.setTo(request.to());
        helper.setSubject(request.subject());
        helper.setText(request.body(), true);

        if (request.documentIds() != null && !request.documentIds().isEmpty()) {

            for (Long documentId : request.documentIds()) {

                Document document = documentRepository.findById(documentId)
                        .orElseThrow(() -> new RuntimeException(
                                "Document not found: " + documentId));

                try (InputStream object = minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket(minioProperties.getBucket())
                                .object(document.getObjectKey())
                                .build())) {

                    byte[] bytes = object.readAllBytes();

                    helper.addAttachment(
                            document.getFileName(),
                            new ByteArrayResource(bytes),
                            document.getContentType()
                    );
                }
            }
        }

        mailSender.send(message);
    }
}