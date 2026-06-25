package org.example.uploadservice.service;

import org.example.uploadservice.entity.UploadedFileProcessingLog;
import org.example.uploadservice.enums.UploadedFileStatus;
import org.example.uploadservice.repository.UploadedFileProcessingLogRepository;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Arrays;

@Service
public class LogProcessingService {

    private final UploadedFileProcessingLogRepository repository;

    public LogProcessingService(UploadedFileProcessingLogRepository repository) {
        this.repository = repository;
    }

    public void processingStarted(Long fileId){
        saveLog(
                fileId,
                UploadedFileStatus.PROCESSING,
                "Processing started",
                null
        );
    }

    public void processingCompleted(Long fileId){
        saveLog(
                fileId,
                UploadedFileStatus.PROCESSED,
                "Processing completed",
                null
        );
    }
    public void processingFailed(Long fileId, Throwable throwable){
        saveLog(
                fileId,
                UploadedFileStatus.FAILED,
                throwable.getMessage(),
                stackTraceToString(throwable)
        );
    }

    private void saveLog(
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

    private String stackTraceToString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        throwable.printStackTrace(pw);

        return sw.toString();
    }
}