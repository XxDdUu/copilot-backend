package com.sky_copilot.ai_copilot.document.repository;
import com.sky_copilot.ai_copilot.document.repository.projection.DocumentChunkProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import com.sky_copilot.ai_copilot.document.entity.DocumentChunk;


@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {

    @Query(value = """
        SELECT
            id,
            document_id AS documentId,
            chunk_index AS chunkIndex,
            content,
            created_at AS createdAt,
            1 - (embedding <=> CAST(:embedding AS vector)) AS score
        FROM document_chunks
        WHERE 1 - (embedding <=> CAST(:embedding AS vector)) >= :threshold
        ORDER BY embedding <=> CAST(:embedding AS vector)
        LIMIT :topK
        """, nativeQuery = true)
    List<DocumentChunkProjection> similaritySearch(
            @Param("embedding") String embedding,
            @Param("threshold") double threshold,
            @Param("topK") int topK
    );
}