package org.example.uploadservice.repository;

import org.example.uploadservice.entity.UploadedFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UploadedFileRepository extends JpaRepository<UploadedFile, Long> {
    List<UploadedFile> findAllByOrderByCreatedAtDesc();
}