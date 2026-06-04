package org.example.uploadservice.service;

import org.example.uploadservice.entity.DocumentChunk;
import org.example.uploadservice.entity.UploadedFile;
import org.example.uploadservice.repository.DocumentChunkRepository;
import org.example.uploadservice.repository.UploadedFileRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentProcessingService {

    private final DocumentChunkRepository chunkRepository;
    private final UploadedFileRepository fileRepository;

    private final Path uploadDir = Path.of("uploads");

    public DocumentProcessingService(
            DocumentChunkRepository chunkRepository,
            UploadedFileRepository fileRepository
    ) {
        this.chunkRepository = chunkRepository;
        this.fileRepository = fileRepository;
    }

    @Async
    public void processFile(UploadedFile uploadedFile) {

        try {
            uploadedFile.setStatus("PROCESSING");
            fileRepository.save(uploadedFile);

            Path filePath = uploadDir.resolve(uploadedFile.getStoredFilename());

            String text = Files.readString(filePath, StandardCharsets.UTF_8);

            List<String> chunks = splitIntoChunks(text, 1000);

            for (int i = 0; i < chunks.size(); i++) {
                DocumentChunk chunk = new DocumentChunk();
                chunk.setFileId(uploadedFile.getId());
                chunk.setChunkIndex(i);
                chunk.setContent(chunks.get(i));
                chunk.setCreatedAt(LocalDateTime.now());

                chunkRepository.save(chunk);
            }

            uploadedFile.setStatus("PROCESSED");
            fileRepository.save(uploadedFile);

            System.out.println("Processing finished for file: " + uploadedFile.getId());

        } catch (Exception e) {

            uploadedFile.setStatus("FAILED");
            fileRepository.save(uploadedFile);

            e.printStackTrace();
        }
    }

    private List<String> splitIntoChunks(String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();

        int start = 0;
        int chunkIndex = 0;

        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());

            String chunk = text.substring(start, end).trim();

            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }

            start = end;
            chunkIndex++;
        }

        return chunks;
    }
}