package org.example.uploadservice.service;

import org.example.uploadservice.dto.ChunkPageDto;
import org.example.uploadservice.entity.DocumentChunk;
import org.example.uploadservice.repository.DocumentChunkRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class ChunkQueryService {

    private final DocumentChunkRepository chunkRepository;

    public ChunkQueryService(DocumentChunkRepository chunkRepository) {
        this.chunkRepository = chunkRepository;
    }

    public ChunkPageDto getChunks(Long fileId, int page, int size) {
        Page<DocumentChunk> chunkPage = chunkRepository.findByFileIdOrderByChunkIndexAsc(
                fileId,
                PageRequest.of(page, size)
        );

        return new ChunkPageDto(
                fileId,
                chunkPage.getContent(),
                chunkPage.getNumber(),
                chunkPage.getSize(),
                chunkPage.getTotalElements(),
                chunkPage.getTotalPages(),
                chunkPage.hasPrevious(),
                chunkPage.hasNext()
        );
    }
}