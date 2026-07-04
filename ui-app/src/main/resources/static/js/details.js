const fileDetails = document.getElementById("fileDetails");

async function openFileDetails(fileId) {
    showSection("detailsSection");

    const chunks = await fetchFileChunks(fileId);
    const logs = await fetchFileLogs(fileId);
    const embeddingStatus = await fetchEmbeddingStatus(fileId);

    renderFileDetails(fileId, chunks, logs, embeddingStatus);
}

function renderFileDetails(fileId, chunks, logs, embeddingStatus) {
    fileDetails.innerHTML = "";

    const title = document.createElement("h3");
    title.textContent = `File ID: ${fileId}`;
    fileDetails.appendChild(title);

    fileDetails.appendChild(renderEmbeddingStatus(embeddingStatus));
    fileDetails.appendChild(renderLogsTable(logs));
    fileDetails.appendChild(renderChunksSection(chunks));
}

function renderEmbeddingStatus(status) {
    const container = document.createElement("div");

    const title = document.createElement("h3");
    title.textContent = "Embedding status";
    container.appendChild(title);

    const table = document.createElement("table");
    table.border = "1";
    table.cellPadding = "6";

    table.innerHTML = `
        <tbody>
        <tr>
            <th>Model</th>
            <td>${status.modelName}</td>
        </tr>
        <tr>
            <th>Chunks</th>
            <td>${status.chunkCount}</td>
        </tr>
        <tr>
            <th>Embeddings</th>
            <td>${status.embeddingCount}</td>
        </tr>
        <tr>
            <th>Missing embeddings</th>
            <td>${status.missingEmbeddingCount}</td>
        </tr>
        <tr>
            <th>Fully embedded</th>
            <td>${status.fullyEmbedded ? "Yes" : "No"}</td>
        </tr>
        </tbody>
    `;

    container.appendChild(table);
    return container;
}

function renderLogsTable(logs) {
    const container = document.createElement("div");

    const title = document.createElement("h3");
    title.textContent = "Processing logs";
    container.appendChild(title);

    if (logs.length === 0) {
        const empty = document.createElement("p");
        empty.textContent = "No logs found.";
        container.appendChild(empty);
        return container;
    }

    const table = document.createElement("table");
    table.border = "1";
    table.cellPadding = "6";

    table.innerHTML = `
        <thead>
        <tr>
            <th>Status</th>
            <th>Message</th>
            <th>Created at</th>
            <th>Stack trace</th>
        </tr>
        </thead>
        <tbody></tbody>
    `;

    const tbody = table.querySelector("tbody");

    for (const log of logs) {
        const row = document.createElement("tr");

        row.innerHTML = `
            <td>${log.status}</td>
            <td>${log.message ?? ""}</td>
            <td>${log.createdAt}</td>
            <td>
                <pre>${log.stackTrace ?? ""}</pre>
            </td>
        `;

        tbody.appendChild(row);
    }

    container.appendChild(table);
    return container;
}

function renderChunksSection(chunks) {
    const container = document.createElement("div");

    const title = document.createElement("h3");
    title.textContent = "Chunks";
    container.appendChild(title);

    if (chunks.length === 0) {
        const empty = document.createElement("p");
        empty.textContent = "No chunks found.";
        container.appendChild(empty);
        return container;
    }

    for (const chunk of chunks) {
        const block = document.createElement("pre");
        block.textContent = `Chunk ${chunk.chunkIndex}\n\n${chunk.content}`;
        container.appendChild(block);
    }

    return container;
}