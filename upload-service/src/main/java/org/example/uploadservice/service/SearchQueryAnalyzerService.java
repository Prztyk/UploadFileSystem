package org.example.uploadservice.service;

import org.example.uploadservice.enums.SearchMode;
import org.springframework.stereotype.Service;

@Service
public class SearchQueryAnalyzerService {

    public SearchMode determineSearchMode(String query) {
        if (query == null || query.isBlank()) {
            return SearchMode.LEXICAL_ONLY;
        }

        String trimmedQuery = query.trim();

        if (isQuotedPhrase(trimmedQuery)) {
            return SearchMode.EXACT_PHRASE;
        }

        if (looksLikeIdentifier(trimmedQuery)) {
            return SearchMode.LEXICAL_ONLY;
        }

        return SearchMode.HYBRID;
    }

    public String normalizeQuery(String query) {
        if (query == null) {
            return "";
        }

        String trimmedQuery = query.trim();

        if (isQuotedPhrase(trimmedQuery)) {
            return trimmedQuery.substring(1, trimmedQuery.length() - 1).trim();
        }

        return trimmedQuery;
    }

    private boolean isQuotedPhrase(String query) {
        return query.length() >= 2
                && query.startsWith("\"")
                && query.endsWith("\"");
    }

    private boolean looksLikeIdentifier(String query) {
        boolean containsWhitespace = query.matches(".*\\s+.*");
        boolean containsDigit = query.matches(".*\\d.*");
        boolean containsSpecialCharacter = query.matches(".*[^a-zA-Z0-9\\s].*");

        return !containsWhitespace && (containsDigit || containsSpecialCharacter);
    }
}