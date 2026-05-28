package org.example.uploadservice.service;

import org.example.uploadservice.entity.UploadedFile;
import org.example.uploadservice.repository.UploadedFileRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class DocumentProcessingService {

    private final UploadedFileRepository repository;

    public DocumentProcessingService(UploadedFileRepository repository) {
        this.repository = repository;
    }

    @Async
    public void processFile(UploadedFile uploadedFile) {

        try {
            uploadedFile.setStatus("PROCESSING");
            repository.save(uploadedFile);

            Thread.sleep(5000);

            uploadedFile.setStatus("PROCESSED");
            repository.save(uploadedFile);

            System.out.println("Processing finished for file: " + uploadedFile.getId());

        } catch (Exception e) {

            uploadedFile.setStatus("FAILED");
            repository.save(uploadedFile);

            e.printStackTrace();
        }
    }
}