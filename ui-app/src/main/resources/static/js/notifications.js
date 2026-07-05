const notificationBanner = document.getElementById("notificationBanner");
const notificationMessage = document.getElementById("notificationMessage");
const notificationCloseButton = document.getElementById("notificationCloseButton");

let notificationTimeoutId = null;

function showNotification(message, type = "info", timeoutMs = 5000) {
    clearNotificationTimeout();

    notificationMessage.textContent = message;

    notificationBanner.classList.remove("hidden", "error", "success", "info");
    notificationBanner.classList.add(type);

    if (timeoutMs > 0) {
        notificationTimeoutId = setTimeout(function () {
            hideNotification();
        }, timeoutMs);
    }
}

function showError(message) {
    showNotification(message, "error", 8000);
}

function showSuccess(message) {
    showNotification(message, "success", 5000);
}

function showInfo(message) {
    showNotification(message, "info", 5000);
}

function hideNotification() {
    clearNotificationTimeout();

    notificationBanner.classList.add("hidden");
    notificationBanner.classList.remove("error", "success", "info");
    notificationMessage.textContent = "";
}

function clearNotificationTimeout() {
    if (notificationTimeoutId !== null) {
        clearTimeout(notificationTimeoutId);
        notificationTimeoutId = null;
    }
}

notificationCloseButton.addEventListener("click", hideNotification);