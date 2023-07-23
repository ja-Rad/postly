function calculateScrollPercentage() {
    const winScroll = document.body.scrollTop || document.documentElement.scrollTop;
    const height = document.documentElement.scrollHeight - document.documentElement.clientHeight;
    const scrolled = (winScroll / height) * 100;
    document.getElementById("myBar").style.width = scrolled + "%";
}

// When the user scrolls the page, execute calculateScrollPercentage
window.onscroll = function () {
    calculateScrollPercentage();
};

function localizeDate() {
    // Select all labels with the 'formatDate' class
    const labels = document.getElementsByClassName("formatDate");

    // Define options for date and time format
    const dateOptions = { day: "numeric", month: "long", year: "numeric" };
    const timeOptions = { hour: "numeric", minute: "numeric", hour12: true };

    // Loop through the labels and update each one
    for (let label of labels) {
        // Convert the label's text content to a Date object
        const date = new Date(label.textContent);

        // Format the date and time as strings in the client's timezone
        const localDateStr = date.toLocaleDateString(undefined, dateOptions);
        const localTimeStr = date.toLocaleTimeString(undefined, timeOptions);

        // Replace the label's text with the local date and time strings
        label.textContent = localDateStr + " at " + localTimeStr;
    }
}

// Call the function
localizeDate();

// Bootstrap Popovers functionality
const popoverHowToFormat = document.getElementById("popover-how-to-format");
const popover = new bootstrap.Popover(popoverHowToFormat, {
    trigger: "focus",
    content: '<p class="p-highlight">Create content Headers with hashes:</p> <p>##like so##</p> <p class="p-highlight">The result:</p> <h4>like so</h4>',
    html: true,
});
