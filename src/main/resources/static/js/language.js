// 1. All translations in one object
const translations = {
    en: {
      titlelang: "skgchess.com - The Greek Chess Platform.",
      motolang: "The first chess platform in Greece.",
      ctalang: "Play",
      signinlang: "Sign in",
      signuplang: "Sign up",
      signoutlang: "Sign out",
      fordeveloperslang: "Custom Chess Engine",
      contactuslang: "Contact us",
      //
      welcomelang: "Welcome, ",
      newusernamelang: "New Username",
      usernameinfomessagelang: "You will be prompted to relogin after changing the username.",
      saveusernamebtnmodal: "Save",
      cancelusernamebtnmodal: "Cancel",
      scrolltolang: "Scroll to explore",
      analyzerecentgamelang: "Analyze Recent Game",
      startagamemessagelang: "Start a game to see your moves and analyze your strategy here!",
      recentgameslang: "Recent Games",
      recentgamesmessagelang: "Your recent games will appear here after playing.",
      playerslang: "Players",
      resultlang: "Result",
      moveslang: "Moves",
      datelang: "Date",
      viewallgameslang: "View All Games",
      pendinginvitationslang: "Pending Invitations",
      gametitlelang: "Play Chess - skgchess.com - The Greek Chess Platform",
      gamesigninlang: "Sign In",
      gameplaytablang: "Play",
      gamemovehistorylang: "Move History",
      gamechatlang: "Game Chat",
      gamechatmodallang: "Game Chat",
      chatmodalinput: "Type a message...",
      chatinput: "Type a message...",
      featureheadinglang : "Ιnspired by Thessaloniki",
      featuredescriptionlang :  "Historical photos, classical music, and unique themes create an experience where retro meets modern, bringing chess closer to the culture of the city.",
      card1lang: "Integrated chat",
      card2lang: "Classical music",
      card3lang: "Game history",
      card4lang: "Multiple themes",
      card5lang: "Mobile functionality",
      card0descriptionlang: "Without the use of external libraries.",
      card1descriptionlang: "Chat with other players during games",
      card2descriptionlang: "Play with classical music in the background.",
      card3descriptionlang: "Optimize your game with the game history.",
      card4descriptionlang: "Choose between three themes.",
      card5descriptionlang: "Play from your computer or mobile.",
      ixllang: "Created by IXLSTUDIO."
    },
    gr: {
      titlelang: "skgchess.com - Η πρώτη σκακιστική πλατφόρμα στην Ελλάδα.",
      motolang: "Η πρώτη σκακιστική πλατφόρμα στην Ελλάδα.",
      ctalang: "Παίξε",  
      signinlang: "Σύνδεση",
      signuplang: "Εγγραφή",
      signoutlang: "Αποσύνδεση",
      fordeveloperslang: "Custom Chess Engine",
      contactuslang: "Επικοινωνήστε μαζί μας",
      //
      welcomelang: "Καλώς ήρθες, ",
      newusernamelang: "Νέο όνομα χρήστη",
      usernameinfomessagelang: "Θα σας ζητηθεί να συνδεθείτε ξανά μετά την αλλαγή του όνοματος χρήστη.",
      saveusernamebtnmodal: "Αποθήκευση",
      cancelusernamebtnmodal: "Ακύρωση",
      scrolltolang: "Κάνε σκρολ για να εξερευνήσεις",
      analyzerecentgamelang: "Ανάλυση τελευταίου παιχνιδιού",
      startagamemessagelang: "Ξεκινήστε ένα παιχνίδι για να δείτε τις κινήσεις σας και να αναλύσετε την στρατηγική σας εδώ!",
      recentgameslang: "Πρόσφατα παιχνίδια",
      recentgamesmessagelang: "Τα πρόσφατα σας παιχνίδια θα εμφανιστούν εδώ μόλις παίξετε.",
      playerslang: "Παίκτες",
      resultlang: "Αποτέλεσμα",
      moveslang: "Κινήσεις",
      datelang: "Ημερομηνία",
      viewallgameslang: "Προβολή όλων των παιχνιδιών",
      pendinginvitationslang: "Προσκλήσεις σε αναμονή",
      gametitlelang: "Πάιξε Σκάκι - skgchess.com - Η ελληνική πλατφόρμα για το σκάκι.",
      gamesigninlang: "Σύνδεση",
      gameplaytablang: "Παιχνίδι",
      gamemovehistorylang: "Ιστορικό Κινήσεων",
      gamechatlang: "Συνομιλία",
      gamechatmodallang: "Συνομιλία",
      chatmodalinput: "Πληκτρολογήστε ένα μήνυμα...",
      chatinput: "Πληκτρολογήστε ένα μήνυμα...",
      featureheadinglang : "Mε άρωμα Θεσσαλονίκης",
      featuredescriptionlang : "Ιστορικές φωτογραφίες, κλασική μουσική και μοναδικά θέματα δημιουργούν μια εμπειρία όπου το ρετρό συναντά το μοντέρνο, φέρνοντας το σκάκι πιο κοντά στην κουλτούρα της πόλης.",
      card1lang: "Ενσωματωμένη συνομιλία",
      card2lang: "Κλασσική μουσική",
      card3lang: "Ιστορικό παιχνιδιών",
      card4lang: "Πολλαπλά θέματα",
      card5lang: "Λειτουργία σε κινητά",
      card0descriptionlang: "Χωρίς την χρήση εξωτερικών βιβλιοθηκών.",
      card1descriptionlang: "Συνομίλησε με άλλους παίκτες κατά τη διάρκεια των παιχνιδιών",
      card2descriptionlang: "Παίξε με κλασσική μουσική στο παρασκήνιο.",
      card3descriptionlang: "Βελτιστοποιήσε το παιχνίδι σου με το ιστορικό παιχνιδιών.",
      card4descriptionlang: "Διάλεξε μεταξύ τριών θεμάτων.",
      card5descriptionlang: "Παίξε απο υπολογιστή ή κινητό.",
      ixllang: "Mια δημιουργία του IXLSTUDIO."
    }
  };
  
  // 2. Function to update all translatable areas
  function updateLanguage(lang) {
    document.title = translations[lang].titlelang;
    for (const key in translations[lang]) {
        const el = document.getElementById(key);
        if (el) el.textContent = translations[lang][key];
    }
    // Set chat input placeholders explicitly after the loop
    const chatInput = document.getElementById('chatinput');
    if (chatInput) chatInput.placeholder = translations[lang].chatinput;
    const chatModalInput = document.getElementById('chatmodalinput');
    if (chatModalInput) chatModalInput.placeholder = translations[lang].chatmodalinput;
    localStorage.setItem('lang', lang);
    document.querySelectorAll('.burger-lang').forEach(span => {
        span.classList.remove('active');
    });
    const activeSpan = document.getElementById('lang-' + lang);
    if (activeSpan) activeSpan.classList.add('active');
}
  
