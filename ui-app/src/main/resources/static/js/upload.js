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
        uploadMessage.textContent = message;
        fileInput.value = "";

        await loadUploadHistory();
        showSection("historySection");
    } catch (error) {
        uploadMessage.textContent = error.message;
    }
}