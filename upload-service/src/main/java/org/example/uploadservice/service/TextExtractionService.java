package org.example.uploadservice.service;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

@Service
public class TextExtractionService {

    private final Tika tika = new Tika();

    public String extractText(Path filePath)
            throws IOException, TikaException {
        return tika.parseToString(filePath);
    }
}