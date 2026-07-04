const historyTableBody = document.querySelector("#historyTable tbody");
const refreshHistoryButton = document.getElementById("refreshHistoryButton");

async function loadUploadHistory() {
    const files = await fetchUploadHistory();
    renderUploadHistory(files);
}

function renderUploadHistory(files) {
    historyTableBody.innerHTML = "";

    for (const file of files) {
        const row = document.createElement("tr");

        row.innerHTML = `
            <td>${file.id}</td>
            <td>${file.originalFilename}</td>
            <td>${file.contentType}</td>
            <td>${file.size}</td>
            <td>${file.status}</td>
            <td>${file.createdAt}</td>
            <td>
                <div class="action-buttons">
                    <button onclick="openFileDetails(${file.id})">Details</button>
                    <button onclick="handleReprocessFile(${file.id})">Reprocess</button>
                    <button onclick="handleDeleteFile(${file.id})">Delete</button>
                </div>
            </td>
        `;

        historyTableBody.appendChild(row);
    }
}

async function handleReprocessFile(fileId) {
    const confirmed = confirm(`Reprocess file ${fileId}? Existing chunks and embeddings will be regenerated.`);

    if (!confirmed) {
        return;
    }

    await reprocessFile(fileId);
    await loadUploadHistory();
    await openFileDetails(fileId);
}

async function handleDeleteFile(fileId) {
    const confirmed = confirm(`Delete file ${fileId}? This will delete the file, chunks, embeddings, and logs.`);

    if (!confirmed) {
        return;
    }

    await deleteFile(fileId);
    await loadUploadHistory();

    fileDetails.innerHTML = "<p>Select a file from upload history.</p>";
    showSection("historySection");
}