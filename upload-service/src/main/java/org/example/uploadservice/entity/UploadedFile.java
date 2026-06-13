package org.example.uploadservice.entity;

import jakarta.persistence.*;
import org.example.uploadservice.enums.UploadedFileStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "uploaded_files")
public class UploadedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originalFilename;

    private String storedFilename;

    private String contentType;

    private Long size;

    @Enumerated(EnumType.STRING)
    private UploadedFileStatus status;

    private LocalDateTime createdAt;

    public UploadedFile() {
    }

    public Long getId() {
        return id;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getStoredFilename() {
        return storedFilename;
    }

    public void setStoredFilename(String storedFilename) {
        this.storedFilename = storedFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public UploadedFileStatus getStatus() {
        return status;
    }

    public void setStatus(UploadedFileStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}