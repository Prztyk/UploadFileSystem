package org.example.uploadservice.service;

import org.example.uploadservice.entity.UploadedFileProcessingLog;
import org.example.uploadservice.enums.UploadedFileStatus;
import org.example.uploadservice.repository.UploadedFileProcessingLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LogProcessingService {

    private final UploadedFileProcessingLogRepository repository;

    public LogProcessingService(UploadedFileProcessingLogRepository repository) {
        this.repository = repository;
    }

    public void log(
            Long fileId,
            UploadedFileStatus status,
            String message,
            String stackTrace
    ) {
        UploadedFileProcessingLog log = new UploadedFileProcessingLog();

        log.setFileId(fileId);
        log.setStatus(status);
        log.setMessage(message);
        log.setStackTrace(stackTrace);
        log.setCreatedAt(LocalDateTime.now());

        repository.save(log);
    }
}