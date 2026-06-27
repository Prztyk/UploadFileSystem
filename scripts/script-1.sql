-- just table queries
select * 
from semantic_search.public.uploaded_files uf 
order by id desc;

select * 
from semantic_search.public.document_chunks dc  
order by id desc;

select * from semantic_search.public.uploaded_file_processing_logs ufpl;

select *
from semantic_search.public.flyway_schema_history
order by installed_rank;

select *
from semantic_search.public.document_chunk_embeddings dce;

select id, chunk_id, model_name, created_at
from semantic_search.public.document_chunk_embeddings
order by id desc;

SELECT model_name, COUNT(*)
FROM document_chunk_embeddings
GROUP BY model_name;

-- all tables

SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
ORDER BY table_name;

-- specific queries

select * 
from semantic_search.public.uploaded_files uf 
left join document_chunks dc on dc.file_id = uf.id 
where dc.file_id = 14;



