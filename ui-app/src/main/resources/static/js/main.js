function initializeApplication() {
    initializeNavigation();

    uploadForm.addEventListener("submit", handleUpload);
    refreshHistoryButton.addEventListener("click", loadUploadHistory);
    searchForm.addEventListener("submit", handleSearch);
    answerForm.addEventListener("submit", handleAnswerQuestion);

    loadUploadHistory();
}

initializeApplication();