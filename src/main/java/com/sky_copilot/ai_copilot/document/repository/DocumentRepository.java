package com.sky_copilot.ai_copilot.document.repository;
import com.sky_copilot.ai_copilot.document.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface DocumentRepository
        extends JpaRepository<Document, Long> {
}
