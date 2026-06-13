package org.example.uploadservice.entity;

import jakarta.persistence.*;
import org.example.uploadservice.enums.UploadedFileStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "uploaded_file_processing_logs")
public class UploadedFileProcessingLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long fileId;

    @Enumerated(EnumType.STRING)
    private UploadedFileStatus status;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(columnDefinition = "TEXT")
    private String stackTrace;

    private LocalDateTime createdAt;

    public UploadedFileProcessingLog() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public UploadedFileStatus getStatus() {
        return status;
    }

    public void setStatus(UploadedFileStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}