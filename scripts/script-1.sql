select * 
from semantic_search.public.uploaded_files uf 
order by id desc;

select * 
from semantic_search.public.document_chunks dc  
order by id desc;

select * from semantic_search.public.uploaded_file_processing_logs ufpl ;

SELECT *
FROM semantic_search.public.flyway_schema_history
ORDER BY installed_rank;

select * 
from semantic_search.public.uploaded_files uf 
left join document_chunks dc on dc.file_id = uf.id 
where dc.file_id = 14;



