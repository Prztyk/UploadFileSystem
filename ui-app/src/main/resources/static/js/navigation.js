function showSection(sectionId) {
    const sections = document.querySelectorAll(".page-section");

    for (const section of sections) {
        section.classList.add("hidden");
    }

    document.getElementById(sectionId).classList.remove("hidden");
}

function initializeNavigation() {
    const menuButtons = document.querySelectorAll(".menu button");

    for (const button of menuButtons) {
        button.addEventListener("click", function () {
            showSection(button.dataset.section);
        });
    }
}