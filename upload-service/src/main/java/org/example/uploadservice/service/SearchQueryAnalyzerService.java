package org.example.uploadservice.service;

import org.springframework.stereotype.Service;

@Service
public class SearchQueryAnalyzerService {

    public boolean shouldRequireLexicalMatch(String query) {
        if (query == null || query.isBlank()) {
            return true;
        }

        String trimmedQuery = query.trim();

        boolean containsWhitespace = trimmedQuery.matches(".*\\s+.*");
        boolean containsDigit = trimmedQuery.matches(".*\\d.*");
        boolean containsSpecialCharacter = trimmedQuery.matches(".*[^a-zA-Z0-9\\s].*");

        return !containsWhitespace && (containsDigit || containsSpecialCharacter);
    }
}