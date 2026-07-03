package org.example.uploadservice.repository;

import org.example.uploadservice.entity.UploadedFileProcessingLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UploadedFileProcessingLogRepository extends JpaRepository<UploadedFileProcessingLog, Long> {

    List<UploadedFileProcessingLog> findByFileIdOrderByCreatedAtAsc(Long fileId);
    void deleteByFileId(Long fileId);
}