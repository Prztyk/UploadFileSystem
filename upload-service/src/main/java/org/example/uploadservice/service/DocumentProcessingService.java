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

            List<String> chunks = splitIntoChunks(text, 1000, 200);

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

    private List<String> splitIntoChunks(String text, int chunkSize, int overlapSize) {
        List<String> chunks = new ArrayList<>();

        if (text == null || text.isBlank()) {
            return chunks;
        }

        String normalizedText = text
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .trim();

        int start = 0;

        while (start < normalizedText.length()) {
            int targetEnd = Math.min(start + chunkSize, normalizedText.length());
            int end = findBestSplitPosition(normalizedText, start, targetEnd);

            String chunk = normalizedText.substring(start, end).trim();

            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }

            if (end >= normalizedText.length()) {
                break;
            }

            start = Math.max(0, end - overlapSize);
        }

        return chunks;
    }

    private int findBestSplitPosition(String text, int start, int targetEnd) {
        if (targetEnd >= text.length()) {
            return text.length();
        }

        int paragraphBreak = text.lastIndexOf("\n\n", targetEnd);
        if (paragraphBreak > start) {
            return paragraphBreak;
        }

        int lineBreak = text.lastIndexOf("\n", targetEnd);
        if (lineBreak > start) {
            return lineBreak;
        }

        int space = text.lastIndexOf(" ", targetEnd);
        if (space > start) {
            return space;
        }

        return targetEnd;
    }
}