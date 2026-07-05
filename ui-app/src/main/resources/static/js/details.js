const fileDetails = document.getElementById("fileDetails");

let currentDetailsFileId = null;
let currentChunksPage = 0;
const chunksPageSize = 10;

async function openFileDetails(fileId) {
    showSection("detailsSection");

    currentDetailsFileId = fileId;
    currentChunksPage = 0;

    fileDetails.innerHTML = "<p>Loading file details...</p>";

    try {
        const details = await fetchFileDetails(fileId);
        renderFileDetails(details);
    } catch (error) {
        fileDetails.innerHTML = `<p class="error-message">${error.message}</p>`;
    }
}

function renderFileDetails(details) {
    fileDetails.innerHTML = "";

    const title = document.createElement("h3");
    title.textContent = `${details.file.originalFilename} — File ID: ${details.file.id}`;
    fileDetails.appendChild(title);

    fileDetails.appendChild(renderFileSummary(details.file));
    fileDetails.appendChild(renderEmbeddingStatus(details.embeddingStatus));
    fileDetails.appendChild(renderLogsTable(details.logs));
    fileDetails.appendChild(renderChunksPlaceholder(details.file.id));
}

function renderChunksPlaceholder(fileId) {
    const container = document.createElement("div");
    container.id = "chunksContainer";

    const title = document.createElement("h3");
    title.textContent = "Chunks";
    container.appendChild(title);

    const button = document.createElement("button");
    button.textContent = "Load chunks";
    button.addEventListener("click", function () {
        loadChunksPage(fileId, 0);
    });

    container.appendChild(button);

    return container;
}

async function loadChunksPage(fileId, page) {
    const chunksContainer = document.getElementById("chunksContainer");

    chunksContainer.innerHTML = "<h3>Chunks</h3><p>Loading chunks...</p>";

    const chunkPage = await fetchFileChunks(fileId, page, chunksPageSize);

    currentChunksPage = chunkPage.page;

    renderChunksPage(chunkPage);
}

function renderChunksPage(chunkPage) {
    const chunksContainer = document.getElementById("chunksContainer");

    chunksContainer.innerHTML = "";

    const title = document.createElement("h3");
    title.textContent = "Chunks";
    chunksContainer.appendChild(title);

    const pageInfo = document.createElement("p");
    pageInfo.textContent =
        `Page ${chunkPage.page + 1} of ${chunkPage.totalPages} ` +
        `(${chunkPage.totalElements} chunks total)`;
    chunksContainer.appendChild(pageInfo);

    const controlsTop = renderChunkPaginationControls(chunkPage);
    chunksContainer.appendChild(controlsTop);

    if (chunkPage.chunks.length === 0) {
        const empty = document.createElement("p");
        empty.textContent = "No chunks found.";
        chunksContainer.appendChild(empty);
        return;
    }

    for (const chunk of chunkPage.chunks) {
        const block = document.createElement("pre");
        block.textContent = `Chunk ${chunk.chunkIndex}\n\n${chunk.content}`;
        chunksContainer.appendChild(block);
    }

    const controlsBottom = renderChunkPaginationControls(chunkPage);
    chunksContainer.appendChild(controlsBottom);
}

function renderChunkPaginationControls(chunkPage) {
    const container = document.createElement("div");
    container.className = "action-buttons";

    const previousButton = document.createElement("button");
    previousButton.textContent = "Previous";
    previousButton.disabled = !chunkPage.hasPrevious;
    previousButton.addEventListener("click", function () {
        loadChunksPage(chunkPage.fileId, chunkPage.page - 1);
    });

    const nextButton = document.createElement("button");
    nextButton.textContent = "Next";
    nextButton.disabled = !chunkPage.hasNext;
    nextButton.addEventListener("click", function () {
        loadChunksPage(chunkPage.fileId, chunkPage.page + 1);
    });

    container.appendChild(previousButton);
    container.appendChild(nextButton);

    return container;
}

function renderFileSummary(file) {
    const container = document.createElement("div");

    const title = document.createElement("h3");
    title.textContent = "File summary";
    container.appendChild(title);

    const table = document.createElement("table");
    table.border = "1";
    table.cellPadding = "6";

    table.innerHTML = `
        <tbody>
        <tr>
            <th>Status</th>
            <td>${file.status}</td>
        </tr>
        <tr>
            <th>Original filename</th>
            <td>${file.originalFilename}</td>
        </tr>
        <tr>
            <th>Stored filename</th>
            <td>${file.storedFilename}</td>
        </tr>
        <tr>
            <th>Content type</th>
            <td>${file.contentType}</td>
        </tr>
        <tr>
            <th>Size</th>
            <td>${file.size}</td>
        </tr>
        <tr>
            <th>Created at</th>
            <td>${file.createdAt}</td>
        </tr>
        </tbody>
    `;

    container.appendChild(table);
    return container;
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