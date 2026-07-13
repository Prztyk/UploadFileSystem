const answerForm = document.getElementById("answerForm");
const answerQuestionInput = document.getElementById("answerQuestion");
const answerLimitInput = document.getElementById("answerLimit");
const answerMinSimilarityInput = document.getElementById("answerMinSimilarity");
const answerResults = document.getElementById("answerResults");

async function handleAnswerQuestion(event) {
    event.preventDefault();

    answerResults.innerHTML = "<p>Generating answer...</p>";

    try {
        const response = await answerQuestion(
            answerQuestionInput.value,
            answerLimitInput.value,
            answerMinSimilarityInput.value
        );

        renderAnswerResponse(response);
    } catch (error) {
        answerResults.innerHTML = "";
        showError(error.message);
    }
}

function renderAnswerResponse(response) {
    answerResults.innerHTML = "";

    const answerTitle = document.createElement("h3");
    answerTitle.textContent = "Answer";
    answerResults.appendChild(answerTitle);

    const answerBlock = document.createElement("pre");
    answerBlock.textContent = response.answer;
    answerResults.appendChild(answerBlock);

    const sourcesTitle = document.createElement("h3");
    sourcesTitle.textContent = "Sources";
    answerResults.appendChild(sourcesTitle);

    if (response.sources.length === 0) {
        const empty = document.createElement("p");
        empty.textContent = "No sources found.";
        answerResults.appendChild(empty);
        return;
    }

    const table = document.createElement("table");
    table.border = "1";
    table.cellPadding = "6";

    table.innerHTML = `
        <thead>
        <tr>
            <th>Source</th>
            <th>File</th>
            <th>Chunk</th>
            <th>Mode</th>
            <th>Match</th>
            <th>Hybrid score</th>
        </tr>
        </thead>
        <tbody></tbody>
    `;

    const tbody = table.querySelector("tbody");

    for (const source of response.sources) {
        const row = document.createElement("tr");

        row.innerHTML = `
            <td>${source.sourceNumber}</td>
            <td>${source.originalFilename}</td>
            <td>${source.chunkIndex}</td>
            <td>${source.searchMode}</td>
            <td>${source.matchType}</td>
            <td>${formatNullableNumber(source.hybridScore)}</td>
        `;

        tbody.appendChild(row);
    }

    answerResults.appendChild(table);
}

function formatNullableNumber(value) {
    if (value === null || value === undefined) {
        return "-";
    }

    return value.toFixed(4);
}