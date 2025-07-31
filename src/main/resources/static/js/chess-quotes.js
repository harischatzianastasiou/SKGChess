    // Chess quotes for the welcome message
    const chessQuotes = [
        // Collection of 20 chess and life wisdom quotes
        // "The ability to play chess is the sign of a gentleman. The ability to play chess well is the sign of a wasted life.",
        "Some games you're the artist, some games you're the canvas.",
        "I don't believe in psychology. I believe in good moves.",
        "When you see a good move, look for a better one.",
        "The Pin is mightier than the sword.",
        "Only the player with the initiative has the right to attack.",
        // "Its just you and your opponent at the board and you're trying to prove something.",
        // "Play the opening like a book, the middle game like a magician, and the endgame like a machine.",
        // "Who is your opponent tonight, tonight I am playing against the Black pieces.",
        "The mistakes are there, waiting to be made.",
        "The defensive power of a pinned piece is only imaginary.",
        "The soul becomes dyed with the colour of its thoughts.",
        // "It's not what happens to you, but how you react to it that matters.",
        "He who laughs at himself never runs out of things to laugh at.",
        // "First, say to yourself what you would be, and then do what you have to do.",
        "We suffer more often in imagination than in reality.",
        "The greatest wealth is a poverty of desires.",
        "While we are postponing, life speeds by."
    ];

    // Function to get a random quote
    function getRandomQuote() {
        const randomIndex = Math.floor(Math.random() * chessQuotes.length);
        return chessQuotes[randomIndex];
    }

    // Function to update the welcome message with a random quote
    function updateWelcomeQuote() {
        const quoteElement = document.getElementById('chessquote');
        if (quoteElement) {
            const quote = getRandomQuote();
            quoteElement.textContent = `"${quote}"`;
        }
    }

    // Update the quote when the page loads
    // On small screens, always show the pin quote
    document.addEventListener('DOMContentLoaded', function() {
        const quoteElement = document.getElementById('chessquote');
        if (window.innerWidth < 600) {
            if (quoteElement) quoteElement.textContent = '"The Pin is mightier than the sword."';
        } else {
            updateWelcomeQuote();
        }
    }); 