const searchForm = document.getElementById("searchForm");
const searchQuery = document.getElementById("searchQuery");
const searchLimit = document.getElementById("searchLimit");
const minSimilarity = document.getElementById("minSimilarity");
const searchResults = document.getElementById("searchResults");

async function handleSearch(event) {
    event.preventDefault();

    const results = await searchDocuments(
        searchQuery.value,
        searchLimit.value,
        minSimilarity.value
    );

    renderSearchResults(results);
}

function renderSearchResults(results) {
    searchResults.innerHTML = "";

    if (results.length === 0) {
        searchResults.textContent = "No results found.";
        return;
    }

    const table = document.createElement("table");
    table.border = "1";
    table.cellPadding = "6";

    table.innerHTML = `
        <thead>
        <tr>
            <th>File</th>
            <th>Chunk</th>
            <th>Similarity</th>
            <th>Distance</th>
            <th>Lexical</th>
            <th>Exact phrase</th>
            <th>Hybrid</th>
            <th>Content</th>
        </tr>
        </thead>
        <tbody></tbody>
    `;

    const tbody = table.querySelector("tbody");

    for (const result of results) {
        const row = document.createElement("tr");

        row.innerHTML = `
            <td>${result.originalFilename}</td>
            <td>${result.chunkIndex}</td>
            <td>${result.similarityScore.toFixed(4)}</td>
            <td>${result.distance.toFixed(4)}</td>
            <td>${result.lexicalScore.toFixed(4)}</td>
            <td>${result.exactPhraseMatch ? "Yes" : "No"}</td>
            <td>${result.hybridScore.toFixed(4)}</td>
            <td>
                ${renderContextBlock("Previous chunk", result.previousContent)}
                ${renderContextBlock("Matching chunk", result.content)}
                ${renderContextBlock("Next chunk", result.nextContent)}
            </td>
        `;

        tbody.appendChild(row);
    }

    searchResults.appendChild(table);
}

function renderContextBlock(title, content) {
    if (!content) {
        return "";
    }

    return `
        <div class="context-block">
            <strong>${title}</strong>
            <pre>${content}</pre>
        </div>
    `;
}