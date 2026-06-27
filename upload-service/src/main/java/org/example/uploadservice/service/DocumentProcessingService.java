package org.example.uploadservice.service;

import org.example.uploadservice.entity.DocumentChunk;
import org.example.uploadservice.entity.UploadedFile;
import org.example.uploadservice.enums.UploadedFileStatus;
import org.example.uploadservice.repository.DocumentChunkRepository;
import org.example.uploadservice.repository.UploadedFileRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentProcessingService {

    private final DocumentChunkRepository chunkRepository;
    private final UploadedFileRepository fileRepository;
    private final TextExtractionService textExtractionService;
    private final LogProcessingService logProcessingService;
    private final EmbeddingGenerationService embeddingGenerationService;
    private final EmbeddingPersistenceService embeddingPersistenceService;

    private static final int CHUNK_SIZE = 1000;
    private static final int CHUNK_OVERLAP = 200;
    private static final int CHUNK_SEARCH_WINDOW = 300;

    private final Path uploadDir = Path.of("uploads");

    public DocumentProcessingService(
            DocumentChunkRepository chunkRepository,
            UploadedFileRepository fileRepository,
            TextExtractionService textExtractionService,
            LogProcessingService logProcessingService,
            EmbeddingGenerationService embeddingGenerationService,
            EmbeddingPersistenceService embeddingPersistenceService
    ) {
        this.chunkRepository = chunkRepository;
        this.fileRepository = fileRepository;
        this.textExtractionService = textExtractionService;
        this.logProcessingService = logProcessingService;
        this.embeddingGenerationService = embeddingGenerationService;
        this.embeddingPersistenceService = embeddingPersistenceService;
    }

    @Async
    public void processFile(UploadedFile uploadedFile) {

        try {
            uploadedFile.setStatus(UploadedFileStatus.PROCESSING);
            fileRepository.save(uploadedFile);
            //saveLog(uploadedFile.getId(), UploadedFileStatus.PROCESSING, "Processing started", null);

            /*
            if (uploadedFile.getOriginalFilename().contains("fail")) {
                throw new RuntimeException("Test processing failure");
            }
            */

            Path filePath = uploadDir.resolve(uploadedFile.getStoredFilename());

            String text = textExtractionService.extractText(filePath);

            List<String> chunks = splitIntoChunks(text, CHUNK_SIZE, CHUNK_OVERLAP, CHUNK_SEARCH_WINDOW);

            for (int i = 0; i < chunks.size(); i++) {
                DocumentChunk chunk = new DocumentChunk();
                chunk.setFileId(uploadedFile.getId());
                chunk.setChunkIndex(i);
                chunk.setContent(chunks.get(i));
                chunk.setCreatedAt(LocalDateTime.now());

                DocumentChunk savedChunk = chunkRepository.save(chunk);

                List<Double> embedding = embeddingGenerationService.generateEmbedding(savedChunk.getContent());

                embeddingPersistenceService.saveEmbedding(
                        savedChunk.getId(),
                        "fake-dev-embedding-v1",
                        embedding
                );
            }

            uploadedFile.setStatus(UploadedFileStatus.PROCESSED);
            fileRepository.save(uploadedFile);

            System.out.println("Processing finished for file: " + uploadedFile.getId());

        } catch (Exception e) {

            uploadedFile.setStatus(UploadedFileStatus.FAILED);
            fileRepository.save(uploadedFile);
            logProcessingService.processingFailed(uploadedFile.getId(), e);
        }
    }

    private List<String> splitIntoChunks(String text, int chunkSize, int overlapSize, int separatorSearchWindow) {
        List<String> chunks = new ArrayList<>();

        if (text == null || text.isBlank()) {
            return chunks;
        }

        if(overlapSize >= chunkSize) {
            throw new IllegalArgumentException("overlapSize must be smaller than chunkSize");
        }

        if(separatorSearchWindow <= 0) {
            throw new IllegalArgumentException("separatorSearchWindow must be greater than 0");
        }

        String normalizedText = text
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .trim();

        int start = 0;

        while (start < normalizedText.length()) {
            int targetEnd = Math.min(start + chunkSize, normalizedText.length());

            int end = findBestSplitPosition(normalizedText, start, targetEnd, separatorSearchWindow);

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

    private int findBestSplitPosition(String text, int start, int targetEnd, int separatorSearchWindow) {
        if (targetEnd >= text.length()) {
            return text.length();
        }

        int windowStart = Math.max(start + 1, targetEnd - separatorSearchWindow);

        //String newChunk = text.substring(start, targetEnd);

        int paragraphBreak = text.lastIndexOf("\n\n", targetEnd);
        if (paragraphBreak >= windowStart) {
            return paragraphBreak;
        }

        int lineBreak = text.lastIndexOf("\n", targetEnd);
        if (lineBreak >= windowStart) {
            return lineBreak;
        }

        int space = text.lastIndexOf(" ", targetEnd);
        if (space >= windowStart) {
            return space;
        }

        return targetEnd;
    }
}