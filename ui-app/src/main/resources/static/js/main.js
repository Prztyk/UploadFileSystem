function initializeApplication() {
    initializeNavigation();

    uploadForm.addEventListener("submit", handleUpload);
    refreshHistoryButton.addEventListener("click", loadUploadHistory);
    searchForm.addEventListener("submit", handleSearch);

    loadUploadHistory();
}

initializeApplication();