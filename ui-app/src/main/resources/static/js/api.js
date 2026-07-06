async function uploadFile(file) {
    const formData = new FormData();
    formData.append("file", file);

    const response = await fetch("/upload", {
        method: "POST",
        body: formData
    });

    if (!response.ok) {
        throw new Error(await parseErrorResponse(response));
    }

    return response.json();
}

async function fetchUploadHistory() {
    const response = await fetch("/files/history");

    if (!response.ok) {
        throw new Error(await parseErrorResponse(response));
    }

    return response.json();
}

async function reprocessFile(fileId) {
    const response = await fetch(`/files/${fileId}/reprocess`, {
        method: "POST"
    });

    if (!response.ok) {
        throw new Error(await parseErrorResponse(response));
    }
}

async function deleteFile(fileId) {
    const response = await fetch(`/files/${fileId}`, {
        method: "DELETE"
    });

    if (!response.ok) {
        throw new Error(await parseErrorResponse(response));
    }
}

async function searchDocuments(query, limit, minSimilarity) {
    const response = await fetch(
        `/search?query=${encodeURIComponent(query)}&limit=${encodeURIComponent(limit)}&minSimilarity=${encodeURIComponent(minSimilarity)}`
    );

    if (!response.ok) {
        throw new Error(await parseErrorResponse(response));
    }

    return response.json();
}

async function fetchFileDetails(fileId) {
    const response = await fetch(`/files/${fileId}/details`);

    if (!response.ok) {
        throw new Error(await parseErrorResponse(response));
    }

    return response.json();
}

async function fetchFileChunks(fileId, page, size) {
    const response = await fetch(
        `/files/${fileId}/chunks?page=${encodeURIComponent(page)}&size=${encodeURIComponent(size)}`
    );

    if (!response.ok) {
        throw new Error(await parseErrorResponse(response));
    }

    return response.json();
}

async function parseErrorResponse(response) {
    const fallbackMessage = `Request failed with status ${response.status}`;

    try {
        const contentType = response.headers.get("content-type") ?? "";

        if (contentType.includes("application/json")) {
            const errorBody = await response.json();

            return errorBody.message
                ?? errorBody.error
                ?? fallbackMessage;
        }

        const text = await response.text();

        return text || fallbackMessage;
    } catch (error) {
        return fallbackMessage;
    }
}