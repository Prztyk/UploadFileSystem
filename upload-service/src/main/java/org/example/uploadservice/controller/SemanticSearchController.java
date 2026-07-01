package org.example.uploadservice.controller;

import org.example.uploadservice.dto.SemanticSearchResultDto;
import org.example.uploadservice.service.SemanticSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SemanticSearchController {

    private final SemanticSearchService semanticSearchService;

    public SemanticSearchController(SemanticSearchService semanticSearchService) {
        this.semanticSearchService = semanticSearchService;
    }

    @GetMapping
    public ResponseEntity<List<SemanticSearchResultDto>> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int limit,
            @RequestParam(defaultValue = "0.0") double minSimilarity
    ) {
        return ResponseEntity.ok(
                semanticSearchService.search(query, limit, minSimilarity)
        );
    }
}