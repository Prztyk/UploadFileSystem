Browser flow:

   Browser
   ↓
   ui-app
   ↓
   upload-service
   ↓
   PostgreSQL

Backend flow:  

    upload request
    ↓
    save file
    ↓
    save metadata
    ↓
    HTTP response returned immediately
    ↓
    background processing starts
    ↓
    extract text with Tika
    ↓
    split into chunks
    ↓
    save chunks
    ↓
    generate fake embedding
    ↓
    save embedding into pgvector table

# changes

1. expose history & chunks through API (commit: 104e5e76)  
test me
   - GET http://localhost:8081/api/files/history
   - GET http://localhost:8081/api/files/1/chunks
2. 

To do:  
- Warning:(36, 15) Call to 'printStackTrace()' should probably be replaced with more robust logging  
E:\Repo\Java\UploadFileSystem\upload-service\src\main\java\org\example\uploadservice\service\DocumentProcessingService.java
- List of uploaded documents
- private final Path uploadDir = Path.of("uploads"); in several places

# Ollama

1. commands
   - pull  
     
         ollama pull nomic-embed-text
   - list  

         ollama list
   - remove model  

         ollama rm nomic-embed-text
    
   - test  
     - Git Bash  
      
           curl http://localhost:11434/api/embeddings -d '{
             "model": "nomic-embed-text",
             "prompt": "The sky is blue because of Rayleigh scattering."
           }'
           
           curl http://localhost:11434/api/embed -d "{\"model\":\"nomic-embed-text\",\"input\":\"This is a test sentence\"}"     

     - Powershell  
     
           Invoke-RestMethod -Method Post -Uri "http://localhost:11434/api/embeddings" -Body (@{
             model = "nomic-embed-text"
             prompt = "The sky is blue because of Rayleigh scattering."
           } | ConvertTo-Json)
2. 

# Notes

### docker commands

1. Stop containers, Remove containers, Remove Docker network, Remove volumes


    docker compose down -v  
    -v means remove volumes
2. Create and start containers from docker-compose.yml, Run them in the background 


    docker compose up -d  

### other

1. EmbeddingPersistenceService  
Used JdbcTemplate instead of JPA here, because VECTOR(1536) is a PostgreSQL/pgvector-specific type.
2. First embedding model nomic-embed-text  
It uses vector dimension 768, so i need to modify database -> V006__document_chunk_embeddings__change_embedding_dimension_to_768.sql