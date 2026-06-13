select * 
from semantic_search.public.uploaded_files uf 
order by id desc;


select * 
from semantic_search.public.uploaded_files uf 
left join document_chunks dc on dc.file_id = uf.id 
where dc.file_id = 14; 