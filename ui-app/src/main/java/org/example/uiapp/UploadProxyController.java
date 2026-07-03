package org.example.uiapp;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class UploadProxyController {

    private final RestClient restClient = RestClient.create("http://localhost:8081");

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileResource);

        String response = restClient.post()
                .uri("/api/files/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(String.class);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/files/history")
    public ResponseEntity<String> getUploadHistory() {
        String response = restClient.get()
                .uri("/api/files/history")
                .retrieve()
                .body(String.class);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/files/{fileId}/chunks")
    public ResponseEntity<String> getChunks(@PathVariable Long fileId) {
        String response = restClient.get()
                .uri("/api/files/{fileId}/chunks", fileId)
                .retrieve()
                .body(String.class);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<String> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int limit,
            @RequestParam(defaultValue = "0.0") double minSimilarity
    ) {
        String response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/search")
                        .queryParam("query", query)
                        .queryParam("limit", limit)
                        .queryParam("minSimilarity", minSimilarity)
                        .build())
                .retrieve()
                .body(String.class);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/files/{fileId}/logs")
    public ResponseEntity<String> getLogs(@PathVariable Long fileId) {
        String response = restClient.get()
                .uri("/api/files/{fileId}/logs", fileId)
                .retrieve()
                .body(String.class);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/files/{fileId}/embedding-status")
    public ResponseEntity<String> getEmbeddingStatus(@PathVariable Long fileId) {
        String response = restClient.get()
                .uri("/api/files/{fileId}/embedding-status", fileId)
                .retrieve()
                .body(String.class);

        return ResponseEntity.ok(response);
    }
}