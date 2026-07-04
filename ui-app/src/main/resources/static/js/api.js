async function uploadFile(file) {
    const formData = new FormData();
    formData.append("file", file);

    const response = await fetch("/upload", {
        method: "POST",
        body: formData
    });

    if (!response.ok) {
        throw new Error("Upload failed");
    }

    return response.text();
}

async function fetchUploadHistory() {
    const response = await fetch("/files/history");

    if (!response.ok) {
        throw new Error("Failed to load upload history");
    }

    return response.json();
}

async function fetchFileChunks(fileId) {
    const response = await fetch(`/files/${fileId}/chunks`);

    if (!response.ok) {
        throw new Error("Failed to load chunks");
    }

    return response.json();
}

async function fetchFileLogs(fileId) {
    const response = await fetch(`/files/${fileId}/logs`);

    if (!response.ok) {
        throw new Error("Failed to load processing logs");
    }

    return response.json();
}

async function fetchEmbeddingStatus(fileId) {
    const response = await fetch(`/files/${fileId}/embedding-status`);

    if (!response.ok) {
        throw new Error("Failed to load embedding status");
    }

    return response.json();
}

async function reprocessFile(fileId) {
    const response = await fetch(`/files/${fileId}/reprocess`, {
        method: "POST"
    });

    if (!response.ok) {
        throw new Error("Failed to reprocess file");
    }
}

async function deleteFile(fileId) {
    const response = await fetch(`/files/${fileId}`, {
        method: "DELETE"
    });

    if (!response.ok) {
        throw new Error("Failed to delete file");
    }
}

async function searchDocuments(query, limit, minSimilarity) {
    const response = await fetch(
        `/search?query=${encodeURIComponent(query)}&limit=${encodeURIComponent(limit)}&minSimilarity=${encodeURIComponent(minSimilarity)}`
    );

    if (!response.ok) {
        throw new Error("Search failed");
    }

    return response.json();
}