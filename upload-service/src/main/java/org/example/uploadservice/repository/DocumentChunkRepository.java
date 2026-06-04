package org.example.uploadservice.repository;

import org.example.uploadservice.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {

    List<DocumentChunk> findByFileIdOrderByChunkIndexAsc(Long fileId);
}