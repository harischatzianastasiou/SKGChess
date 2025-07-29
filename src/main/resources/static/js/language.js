// 1. All translations in one object
const translations = {
    en: {
      titlelang: "ToSkaki.GR - The Greek Chess Platform.",
      motolang: "The first chess platform in Greece.",
      ctalang: "Join",
      signinlang: "Sign in",
      signuplang: "Sign up",
      signoutlang: "Sign out",
      fordeveloperslang: "For developers",
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
      gametitlelang: "Play Chess - ToSkaki.GR The Greek Chess Platform",
      gamesigninlang: "Sign In",
      gameplaytablang: "Play",
      gamemovehistorylang: "Move History",
      gamechatlang: "Game Chat",
      gamechatmodallang: "Game Chat",
      chatmodalinput: "Type a message...",
      chatinput: "Type a message..."
    },
    gr: {
      titlelang: "ToSkaki.GR - Η πρώτη σκακιστική πλατφόρμα στην Ελλάδα.",
      motolang: "Η πρώτη σκακιστική πλατφόρμα στην Ελλάδα.",
      ctalang: "Γίνε Μέλος",
      signinlang: "Σύνδεση",
      signuplang: "Εγγραφή",
      signoutlang: "Αποσύνδεση",
      fordeveloperslang: "Για προγραμματιστές",
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
      gametitlelang: "Πάιξε Σκάκι - ToSkaki.GR - Η ελληνική πλατφόρμα για το σκάκι.",
      gamesigninlang: "Σύνδεση",
      gameplaytablang: "Παιχνίδι",
      gamemovehistorylang: "Ιστορικό Κινήσεων",
      gamechatlang: "Συνομιλία",
      gamechatmodallang: "Συνομιλία",
      chatmodalinput: "Πληκτρολογήστε ένα μήνυμα...",
      chatinput: "Πληκτρολογήστε ένα μήνυμα..."
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
  
