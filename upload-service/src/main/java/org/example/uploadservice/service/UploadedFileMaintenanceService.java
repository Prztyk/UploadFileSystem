package org.example.uploadservice.service;

import org.example.uploadservice.entity.UploadedFile;
import org.example.uploadservice.enums.UploadedFileStatus;
import org.example.uploadservice.repository.DocumentChunkRepository;
import org.example.uploadservice.repository.UploadedFileProcessingLogRepository;
import org.example.uploadservice.repository.UploadedFileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class UploadedFileMaintenanceService {

    private final UploadedFileRepository fileRepository;
    private final DocumentChunkRepository chunkRepository;
    private final UploadedFileProcessingLogRepository logRepository;
    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;
    private final DocumentProcessingService documentProcessingService;
    private final Path uploadDirectory;

    public UploadedFileMaintenanceService(
            UploadedFileRepository fileRepository,
            DocumentChunkRepository chunkRepository,
            UploadedFileProcessingLogRepository logRepository,
            JdbcTemplate jdbcTemplate,
            TransactionTemplate transactionTemplate,
            DocumentProcessingService documentProcessingService,
            @Value("${file-storage.upload-directory}") String uploadDirectory
    ) {
        this.fileRepository = fileRepository;
        this.chunkRepository = chunkRepository;
        this.logRepository = logRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
        this.documentProcessingService = documentProcessingService;
        this.uploadDirectory = Path.of(uploadDirectory);
    }

    public void deleteFile(Long fileId) {
        UploadedFile uploadedFile = transactionTemplate.execute(status -> {
            UploadedFile file = findUploadedFile(fileId);

            deleteEmbeddingsForFile(fileId);
            chunkRepository.deleteByFileId(fileId);
            logRepository.deleteByFileId(fileId);
            fileRepository.delete(file);

            return file;
        });

        deleteStoredFile(uploadedFile);
    }

    public void reprocessFile(Long fileId) {
        UploadedFile uploadedFile = transactionTemplate.execute(status -> {
            UploadedFile file = findUploadedFile(fileId);

            deleteEmbeddingsForFile(fileId);
            chunkRepository.deleteByFileId(fileId);
            logRepository.deleteByFileId(fileId);

            file.setStatus(UploadedFileStatus.UPLOADED);
            return fileRepository.save(file);
        });

        documentProcessingService.processFile(uploadedFile);
    }

    private UploadedFile findUploadedFile(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("Uploaded file not found: " + fileId));
    }

    private void deleteEmbeddingsForFile(Long fileId) {
        jdbcTemplate.update(
                """
                DELETE FROM document_chunk_embeddings dce
                USING document_chunks dc
                WHERE dce.chunk_id = dc.id
                  AND dc.file_id = ?
                """,
                fileId
        );
    }

    private void deleteStoredFile(UploadedFile uploadedFile) {
        Path storedFilePath = uploadDirectory.resolve(uploadedFile.getStoredFilename());

        try {
            Files.deleteIfExists(storedFilePath);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to delete stored file: " + storedFilePath, e);
        }
    }
}