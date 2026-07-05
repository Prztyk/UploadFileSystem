const uploadForm = document.getElementById("uploadForm");
const fileInput = document.getElementById("fileInput");
const uploadMessage = document.getElementById("uploadMessage");

async function handleUpload(event) {
    event.preventDefault();

    const file = fileInput.files[0];

    if (!file) {
        uploadMessage.textContent = "Please select a file.";
        return;
    }

    uploadMessage.textContent = "Uploading...";

    try {
        const message = await uploadFile(file);

        uploadMessage.textContent = "";
        fileInput.value = "";

        showSuccess(message || "File uploaded successfully.");

        await loadUploadHistory();
        showSection("historySection");
    } catch (error) {
        uploadMessage.textContent = "";
        showError(error.message)
    }
}