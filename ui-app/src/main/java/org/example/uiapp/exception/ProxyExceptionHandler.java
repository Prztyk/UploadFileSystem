package org.example.uiapp.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;

@RestControllerAdvice
public class ProxyExceptionHandler {

    @ExceptionHandler(HttpStatusCodeException.class)
    public ResponseEntity<String> handleHttpStatusCodeException(HttpStatusCodeException exception) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return ResponseEntity
                .status(exception.getStatusCode())
                .headers(headers)
                .body(exception.getResponseBodyAsString());
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<String> handleResourceAccessException(ResourceAccessException exception) {
        return ResponseEntity
                .status(503)
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "status": 503,
                          "error": "Service Unavailable",
                          "message": "Upload service is not available"
                        }
                        """);
    }
}