    // Multilingual chess quotes for the welcome message
    const chessQuotes = {
        en: [
            // English chess and life wisdom quotes
            "Some games you're the artist, some games you're the canvas.",
            "I don't believe in psychology. I believe in good moves.",
            "When you see a good move, look for a better one.",
            "The Pin is mightier than the sword.",
            "Only the player with the initiative has the right to attack.",
            "The mistakes are there, waiting to be made.",
            "The defensive power of a pinned piece is only imaginary.",
            "The soul becomes dyed with the colour of its thoughts.",
            "He who laughs at himself never runs out of things to laugh at.",
            "We suffer more often in imagination than in reality.",
            "The greatest wealth is a poverty of desires.",
            "While we are postponing, life speeds by."
        ],
        gr: [
            // Greek chess and life wisdom quotes
            "Κάποια παιχνίδια είσαι ο καλλιτέχνης, κάποια είσαι ο καμβάς.",
            "Δεν πιστεύω στην ψυχολογία. Πιστεύω στις καλές κινήσεις.",
            "Όταν βλέπεις μια καλή κίνηση, ψάξε για μια καλύτερη.",
            "The Pin is mightier than the sword.",
            "Μόνο ο παίκτης με την πρωτοβουλία έχει το δικαίωμα να επιτεθεί.",
            "Τα λάθη είναι εκεί, περιμένοντας να γίνουν.",
            "The defensive power of a pinned piece is only imaginary.",
            "The soul becomes dyed with the colour of its thoughts.",
            "Αυτός που γελάει με τον εαυτό του δεν τελειώνουν ποτέ τα πράγματα για να γελάσει.",
            "Υποφέρουμε πιο συχνά στη φαντασία παρά στην πραγματικότητα.",
            "Ο μεγαλύτερος πλούτος είναι η φτώχεια των επιθυμιών.",
            "Ενώ αναβάλλουμε, η ζωή περνάει γρήγορα."
        ]
    };

    // Function to get current language (same as in language.js)
    function getCurrentLanguage() {
        // First, try to get language from URL path
        const pathSegments = window.location.pathname.split('/').filter(segment => segment !== '');
        if (pathSegments.length > 0 && (pathSegments[0] === 'en' || pathSegments[0] === 'gr')) {
            return pathSegments[0];
        }
        
        // Fallback to localStorage
        return localStorage.getItem('lang') || 'gr';
    }

    // Function to get a random quote based on current language
    function getRandomQuote() {
        const currentLang = getCurrentLanguage();
        const quotes = chessQuotes[currentLang] || chessQuotes.gr; // Default to Greek if language not found
        const randomIndex = Math.floor(Math.random() * quotes.length);
        return quotes[randomIndex];
    }

    // Function to update the welcome message with a random quote
    function updateWelcomeQuote() {
        const quoteElement = document.getElementById('chessquote');
        if (quoteElement) {
            const quote = getRandomQuote();
            quoteElement.textContent = `"${quote}"`;
        }
        
        // Also update the quote-text-auth element if it exists
        const quoteTextAuthElement = document.getElementById('quote-text-auth');
        if (quoteTextAuthElement) {
            const quote = getRandomQuote();
            quoteTextAuthElement.textContent = quote; // No extra quotes since CSS adds them
        }
    }

    // Function to get the pin quote in current language
    function getPinQuote() {
        const currentLang = getCurrentLanguage();
        if (currentLang === 'en') {
            return '"The Pin is mightier than the sword."';
        } else {
            return '"Η Καρφίτσα είναι πιο δυνατή από το σπαθί."';
        }
    }

    // Function to update quotes when language changes
    function updateQuotesOnLanguageChange() {
        const quoteElement = document.getElementById('chessquote');
        const quoteTextAuthElement = document.getElementById('quote-text-auth');
        
        if (window.innerWidth < 600) {
            // On small screens, show the pin quote in current language
            if (quoteElement) {
                quoteElement.textContent = getPinQuote();
            }
            if (quoteTextAuthElement) {
                const pinQuote = getPinQuote().replace(/"/g, ''); // Remove quotes since CSS adds them
                quoteTextAuthElement.textContent = pinQuote;
            }
        } else {
            // On larger screens, show a random quote in current language
            updateWelcomeQuote();
        }
    }

    // Update the quote when the page loads
    // On small screens, always show the pin quote
    document.addEventListener('DOMContentLoaded', function() {
        updateQuotesOnLanguageChange();
    });

    // Make the function globally available so it can be called from language.js
    window.updateQuotesOnLanguageChange = updateQuotesOnLanguageChange; 