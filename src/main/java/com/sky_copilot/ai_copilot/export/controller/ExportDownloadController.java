package com.sky_copilot.ai_copilot.export.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/exports")
@RequiredArgsConstructor
public class ExportDownloadController {

    @Value("${export.storage.path:./exports}")
    private String exportStoragePath;

    @GetMapping("/download/{fileId}/{fileName}")
    public ResponseEntity<Resource> download(
            @PathVariable String fileId,
            @PathVariable String fileName) throws IOException {

        Path path = Paths.get(exportStoragePath, fileId);

        Resource resource = new UrlResource(path.toUri());

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }
}