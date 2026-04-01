package rag_document_assistant.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import rag_document_assistant.model.DocumentChunk;

import java.util.List;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {
    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO document_chunk (content, embedding)
        VALUES (:content, CAST(:embedding AS vector))
        """, nativeQuery = true)
    void insertChunk(@Param("content") String content,
                     @Param("embedding") String embedding);



//    @Query(value= """
//            SELECT content FROM document_chunk
//            ORDER BY embedding <-> CAST(:embedding AS vector)
//            LIMIT 8
//            """,nativeQuery = true)
//    List<String> findTopChunks(@Param("embedding") String embedding);
    @Modifying
    @Query(value = """
    SELECT content
    FROM document_chunk
    WHERE content ILIKE CONCAT('%', :keyword, '%')
       OR TRUE
    ORDER BY embedding <-> CAST(:embedding AS vector)
    LIMIT 8
    """, nativeQuery = true)
    List<String> findTopChunksHybrid(@Param("embedding") String embedding,
                                     @Param("keyword") String keyword);
}