function calculateScrollPercentage() {
    var winScroll = document.body.scrollTop || document.documentElement.scrollTop;
    var height = document.documentElement.scrollHeight - document.documentElement.clientHeight;
    var scrolled = (winScroll / height) * 100;
    document.getElementById("myBar").style.width = scrolled + "%";
}

// When the user scrolls the page, execute calculateScrollPercentage
window.onscroll = function () {
    calculateScrollPercentage();
};

function localizeDate() {
    // Select all labels with the 'formatDate' class
    var labels = document.getElementsByClassName("formatDate");

    // Define options for date and time format
    var dateOptions = { day: "numeric", month: "long", year: "numeric" };
    var timeOptions = { hour: "numeric", minute: "numeric", hour12: true };

    // Loop through the labels and update each one
    for (var i = 0; i < labels.length; i++) {
        // Convert the label's text content to a Date object
        var date = new Date(labels[i].textContent);

        // Format the date and time as strings in the client's timezone
        var localDateStr = date.toLocaleDateString(undefined, dateOptions);
        var localTimeStr = date.toLocaleTimeString(undefined, timeOptions);

        // Replace the label's text with the local date and time strings
        labels[i].textContent = localDateStr + " at " + localTimeStr;
    }
}

// Call the function
localizeDate();
