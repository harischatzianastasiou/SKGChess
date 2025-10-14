// 1. All translations in one object
const translations = {
    en: {
      titlelang: "skgchess.com - Thessaloniki’s Chess.",
      motolang: "Thessaloniki’s Chess.",
      ctalang: "Play",
      signinlang: "Sign in",
      signuplang: "Sign up",
      signoutlang: "Sign out",
      fordeveloperslang: "Newspaper",
      contactuslang: "Contact",
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
      gametitlelang: "Play Chess - skgchess.com - Thessaloniki's chess",
      gamesigninlang: "Sign In",
      gameplaytablang: "Play",
      gamemovehistorylang: "Move History",
      gamechatlang: "Game Chat",
      gamechatmodallang: "Game Chat",
      chatmodalinput: "Type a message...",
      chatinput: "Type a message...",
      featureheadinglang : "Τravel through time",
      featuredescriptionlang :  "With the historic White Tower of 1912 and classical music in the background. A unique experience that combines chess with the history of Thessaloniki.",
      card0lang: "Custom chess engine",
      card1lang: "Integrated chat",
      card2lang: "Classical music",
      card3lang: "Game history",
      card4lang: "Multiple themes",
      card5lang: "Chess Quotes",
      card0descriptionlang: "We use our own algorithm for move calculation",
      card1descriptionlang: "Chat with your opponent",
      card2descriptionlang: "Listen to classical music while you play",
      card3descriptionlang: "Revisit all your past games in the home page",
      card4descriptionlang: "Choose between three themes",
      card5descriptionlang: "Enjoy a collection of chess quotes for inspiration",
      ixllang: "Created by IXLSTUDIO.",
      darkModeText: "Features",
      visitIXLSTUDIOlang: " Learn More",
      newGameLang: "New Game",
      joinGameLang: "Join Game",
      allGamesLang: "All Games",
      newGamePopUpLang: "Create New Game",
      // Popup translations
      createNewGameTitle: "Create New Game",
      joinGameTitle: "Join Game",
      pendingInvitationsTitle: "Pending Invitations",
      noPendingInvitations: "No pending invitations",
      loadingInvitations: "Loading invitations...",
      errorLoadingInvitations: "Error loading invitations",
      cancelGameCreation: "Cancel Game Creation?",
      areYouSureCancel: "Are you sure you want to cancel game creation?",
      yesCancel: "Yes, Cancel",
      keepCreating: "Keep Creating",
      sendInvitationTo: "Send Invitation to",
      sendInvitation: "Send Invitation",
      noUsersFound: "No users found",
      errorSearchingUsers: "Error searching users",
      copied: "Copied!",
      copyLink: "Copy Link",
      close: "Close",
      // Game popup translations
      resignGame: "Resign Game",
      areYouSureResign: "Are you sure you want to resign? This will end the game and your opponent will win.",
      resign: "Resign",
      cancel: "Cancel",
      offerDraw: "Offer Draw",
      areYouSureDraw: "Are you sure you want to offer a draw to your opponent?",
      offerDrawBtn: "Offer Draw",
      drawOfferTitle: "Draw Offer",
      drawOfferMessage: "has offered a draw.",
      acceptDraw: "Accept Draw",
      declineDraw: "Decline Draw",
      // Game end popup
      gameEndTitle: "Game Over",
      gameEndSubtitle: "The game has ended",
      offerRematch: "Offer Rematch",
      newGame: "New Game",
      // Error messages
      errorTitle: " ",
      successTitle: " ",
      ok: "OK",
      // Login popup
      loginRequired: "Login Required",
      loginToPlay: "Please log in to play chess",
      login: "Login",
      // Invitation popup
      gameInvitation: "Game Invitation",
      hasInvitedYou: "has invited you to play a game",
      accept: "Accept",
      decline: "Decline",
      // Login/Signup modal
      loginToChess: "Log in",
      continueWithGoogle: "Continue with Google",
      or: "or",
      emailOrUsername: "Email or username",
      emailOrUsernamePlaceholder: "Email or username",
      password: "Password",
      passwordPlaceholder: "Password",
      logIn: "Log In",
      dontHaveAccount: "Don't have an account?",
      signUp: "Sign up",
      forgotPassword: "Forgot your password?",
      signUpForChess: "Sign up",
      username: "Username",
      chooseUsername: "Choose a username",
      email: "Email",
      enterEmail: "Enter your email",
      createPassword: "Create a password",
      confirmPassword: "Confirm Password",
      confirmYourPassword: "Confirm your password",
      signUpBtn: "Sign Up",
      alreadyHaveAccount: "Already have an account?",
      logInLink: "Log in",
      loggingIn: "Logging in...",
      // New game options
      searchForOpponent: "Search for opponent:",
      enterUsernameToSearch: "Enter username to search...",
      timeControl: "Time Control:",
      selectTimeControl: "Select time control",
      yourColor: "Your Color:",
      white: "White",
      black: "Black",
      random: "Random",
      bullet: "Bullet",
      blitz: "Blitz",
      rapid: "Rapid",
      // Notifications
      viewInvitations: "View Invitations",
      dismiss: "Dismiss",
      // Error messages
      pleaseEnterUsername: "Please enter your username or email",
      pleaseEnterPassword: "Please enter your password",
      passwordsDoNotMatch: "Passwords do not match",
      usernameLength: "Username must be between 3 and 50 characters",
      validEmail: "Please enter a valid email address",
      invalidCredentials: "Invalid username or password. Please check your credentials and try again.",
      accountDisabled: "Your account has been disabled. Please contact support for assistance.",
      accountLocked: "Your account has been locked. Please contact support for assistance.",
      authenticationFailed: "Authentication failed. Please check your credentials and try again.",
      errorDuringLogin: "An error occurred during login. Please try again.",
      signupSuccessful: "Signup successful but login failed. Please try logging in manually.",
      accessDenied: "Access denied. Please try again later.",
      invalidInput: "Invalid input. Please check your details and try again.",
      errorCreatingGame: "Error creating game:",
      // Invitation notifications
      invitedYouToGame: "invited you to a",
      minuteGame: "minute game!",
      youWillPlayAs: "You will play as",
      color: "color",
      youWillBeRedirected: "You will be redirected to the page when it is accepted",
      // Notification format
      from: "From",
      invitationToGame: "Invitation to",
      // Time control strings
      minute: "minute",
      minutes: "minute",
      second: "second",
      seconds: "seconds",
      // Game results
      youWon: "You won",
      youLost: "You lost",
      gameEndedInDraw: "Game ended in draw",
      byResignation: "by resignation",
      byCheckmate: "by checkmate",
      byTimeout: "by timeout",
      // Match success/error messages
      matchCreatedSuccessfully: "Match created successfully",
      matchJoinedSuccessfully: "Match joined successfully",
      matchCreationFailed: "Match creation failed",
      matchJoinFailed: "Match join failed",
      invitationSent: "Invitation sent successfully to ",
      invitationAccepted: "Invitation accepted",
      invitationDeclined: "Invitation declined",
      // Additional invitation strings
      accept: "Accept",
      decline: "Decline",
      // Redirect messages
      redirectingToGame: "Redirecting to game...",
      // Invitation error messages
      cannotInviteYourself: "You cannot invite yourself to a game",
      alreadyHaveActiveGame: "You already have an active game",
      opponentHasActiveGame: "The opponent already has an active game",
      pendingInvitationExists: "You already have a pending invitation with this user",
      invitationNotFound: "Invitation not found",
      canOnlyRespondToYourInvitations: "You can only respond to invitations sent to you",
      invitationNoLongerPending: "This invitation is no longer pending",
      invitationExpired: "This invitation has expired",
      invalidAction: "Invalid action. Use 'accept' or 'decline'",
      canOnlyCancelYourInvitations: "You can only cancel invitations you sent",
      // Game error messages
      canOnlyResignWhenInProgress: "You can only resign when the game is in progress",
      canOnlyOfferDrawWhenInProgress: "You can only offer a draw when the game is in progress",
      canOnlyOfferDrawOnYourTurn: "You can only offer a draw on your turn",
      failedToResignGame: "Failed to resign game",
      failedToOfferDraw: "Failed to offer draw",
      failedToRespondToDrawOffer: "Failed to respond to draw offer",
      failedToOfferRematch: "Failed to offer rematch",
      failedToRespondToRematchOffer: "Failed to respond to rematch offer",
      // Game success messages
      rematchOfferSent: "Rematch offer sent to your opponent",
      drawOfferSent: "Draw offer sent to your opponent",
      gameResignedSuccessfully: "Game resigned successfully",
      rematchAccepted: "Rematch accepted",
      // Popup button translations
      successTitle: " ",
      okButton: "OK",
      // Login/Logout messages
      successfullyLoggedIn: "Successfully logged in",
      successfullyLoggedOut: "You have been successfully logged out",
      // Game result messages
      youWon: "You won",
      youLost: "You lost",
      whiteWins: "White Wins",
      blackWins: "Black Wins",
      gameEndedInDraw: "Game ended in draw",
      draw: "Draw",
      byResignation: "by resignation",
      byCheckmate: "by checkmate",
      byTimeout: "by timeout",
      byStalemate: "by stalemate",
      byAgreement: "by agreement",
      gameOver: "Game Over",
      checkmate: "Checkmate",
      stalemate: "Stalemate",
      timeout: "Timeout",
      resignation: "Resignation",
      // Game interface messages
      noMovesYet: "No moves yet",
      // Username change modal
      changeusernamelang: "Change Username",
      newUsername: "New Username",
      save: "Save",
      cancel: "Cancel",
      // Music toggle
      toggleMusic: "Toggle Music",
      musicOn: "Music",
      musicOff: "Music"
    },
    gr: {
      titlelang: "skgchess.com - Το σκάκι της Θεσσαλονίκης",
      motolang: "Το σκάκι της Θεσσαλονίκης.",
      ctalang: "Παίξτε",  
      signinlang: "Σύνδεση",
      signuplang: "Εγγραφή",
      signoutlang: "Αποσύνδεση",
      fordeveloperslang: "Εφημερίδα SKG Chess",
      contactuslang: "Επικοινωνία",
      //
      welcomelang: "Καλώς ήρθες, ",
      newusernamelang: "Νέο όνομα χρήστη",
      usernameinfomessagelang: "Θα σας ζητηθεί να συνδεθείτε ξανά μετά την αλλαγή του όνοματος χρήστη.",
      saveusernamebtnmodal: "Αποθήκευση",
      cancelusernamebtnmodal: "Ακύρωση",
      scrolltolang: " ",
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
      gametitlelang: "Πάιξε Σκάκι - skgchess.com - Το σκάκι της Θεσσαλονίκης.",
      gamesigninlang: "Σύνδεση",
      gameplaytablang: "Παιχνίδι",
      gamemovehistorylang: "Ιστορικό Κινήσεων",
      gamechatlang: "Συνομιλία",
      gamechatmodallang: "Συνομιλία",
      chatmodalinput: "Πληκτρολογήστε ένα μήνυμα...",
      chatinput: "Πληκτρολογήστε ένα μήνυμα...",
      featureheadinglang : "Ταξιδέψτε στον χρόνο",
      featuredescriptionlang : "Με φόντο τον λευκό πύργο του 1912 και διάσημα έργα της κλασικής μουσικής. Μια ξεχωριστή εμπειρία που συνδυάζει το σκάκι με την ιστορία της Θεσσαλονίκης.",
      card0lang: "Aνεξάρτητη μηχανή υπολογισμού κινήσεων",
      card1lang: "Ενσωματωμένη συνομιλία",
      card2lang: "Κλασσική μουσική",
      card3lang: "Ιστορικό παιχνιδιών",
      card4lang: "Πολλαπλά θέματα",
      card5lang: "Σκακιστικά αποφθέγματα",
      card0descriptionlang: "Χρησιμοποιούμε έναν δικό μας αλγόριθμο για τον υπολογισμό των κινήσεων",
      card1descriptionlang: "Δυνατότητα συνομιλίας κατά τη διάρκεια του παιχνιδίου",
      card2descriptionlang: "Συνοδεύστε το παιχνίδι σας με κλασσική μουσική στο παρασκήνιο",
      card3descriptionlang: "Βρείτε όλα τα παιχνίδια σας στην αρχική σελίδα",
      card4descriptionlang: "Επιλέξτε μεταξύ τριών θεμάτων",
      card5descriptionlang: "Απολαύστε μια συλλογή απο σκακιστικά αποφθέγματα",
      ixllang: "Mια δημιουργία του IXLSTUDIO.",
      darkModeText : "Λειτουργίες",
      visitIXLSTUDIOlang: " Μάθετε περισσότερα",
      newGameLang: "Νέο Παιχνίδι",
      joinGameLang: "Συμμετοχή",
      allGamesLang: "Ιστορικό Παιχνιδίων",
      newGamePopUpLang: "Δημιουργία Νέου Παιχνιδιού",
      // Popup translations
      createNewGameTitle: "Δημιουργία Νέου Παιχνιδιού",
      joinGameTitle: "Συμμετοχή σε Παιχνίδι",
      pendingInvitationsTitle: "Προσκλήσεις σε Αναμονή",
      noPendingInvitations: "Δεν υπάρχουν προσκλήσεις σε αναμονή",
      loadingInvitations: "Φόρτωση προσκλήσεων...",
      errorLoadingInvitations: "Σφάλμα φόρτωσης προσκλήσεων",
      cancelGameCreation: "Ακύρωση Δημιουργίας Παιχνιδιού;",
      areYouSureCancel: "Είστε σίγουροι ότι θέλετε να ακυρώσετε τη δημιουργία παιχνιδιού;",
      yesCancel: "Ναι, Ακύρωση",
      keepCreating: "Συνέχεια Δημιουργίας",
      sendInvitationTo: "Αποστολή Πρόσκλησης σε",
      sendInvitation: "Αποστολή Πρόσκλησης",
      noUsersFound: "Δεν βρέθηκαν χρήστες",
      errorSearchingUsers: "Σφάλμα αναζήτησης χρηστών",
      copied: "Αντιγράφηκε!",
      copyLink: "Αντιγραφή Συνδέσμου",
      close: "Κλείσιμο",
      // Game popup translations
      resignGame: "Παραίτηση Παιχνιδιού",
      areYouSureResign: "Είστε σίγουροι ότι θέλετε να παραιτηθείτε; Αυτό θα τερματίσει το παιχνίδι και ο αντίπαλός σας θα κερδίσει.",
      resign: "Παραίτηση",
      cancel: "Ακύρωση",
      offerDraw: "Πρόταση Ισοπαλίας",
      areYouSureDraw: "Είστε σίγουροι ότι θέλετε να προτείνετε ισοπαλία στον αντίπαλό σας;",
      offerDrawBtn: "Πρόταση Ισοπαλίας",
      drawOfferTitle: "Πρόταση Ισοπαλίας",
      drawOfferMessage: "πρότεινε ισοπαλία.",
      acceptDraw: "Αποδοχή Ισοπαλίας",
      declineDraw: "Απόρριψη Ισοπαλίας",
      // Game end popup
      gameEndTitle: "Τέλος Παιχνιδιού",
      gameEndSubtitle: "Το παιχνίδι έχει τελειώσει",
      offerRematch: "Πρόταση Επανάληψης",
      newGame: "Νέο Παιχνίδι",
      // Error messages
      errorTitle: " ",
      successTitle: " ",
      ok: "Εντάξει",
      // Login popup
      loginRequired: "Απαιτείται Σύνδεση",
      loginToPlay: "Παρακαλώ συνδεθείτε για να παίξετε σκάκι",
      login: "Σύνδεση",
      // Invitation popup
      gameInvitation: "Πρόσκληση Παιχνιδιού",
      hasInvitedYou: "Σας προσκάλεσε να παίξετε ένα παιχνίδι",
      accept: "Αποδοχή",
      decline: "Απόρριψη",
      // Login/Signup modal
      loginToChess: "Σύνδεση",
      continueWithGoogle: "Συνέχεια με Google",
      or: "ή",
      emailOrUsername: "Email ή όνομα χρήστη",
      emailOrUsernamePlaceholder: "Email ή όνομα χρήστη",
      password: "Κωδικός",
      passwordPlaceholder: "Κωδικός",
      rememberMe: "Θυμήσου με",
      logIn: "Σύνδεση",
      dontHaveAccount: "Δεν έχετε λογαριασμό;",
      signUp: "Εγγραφή",
      forgotPassword: "Ξεχάσατε τον κωδικό σας;",
      signUpForChess: "Εγγραφή",
      username: "Όνομα χρήστη",
      chooseUsername: "Επιλέξτε όνομα χρήστη",
      email: "Email",
      enterEmail: "Εισάγετε το email σας",
      createPassword: "Δημιουργήστε κωδικό",
      confirmPassword: "Επιβεβαίωση Κωδικού",
      confirmYourPassword: "Επιβεβαιώστε τον κωδικό σας",
      signUpBtn: "Εγγραφή",
      alreadyHaveAccount: "Έχετε ήδη λογαριασμό;",
      logInLink: "Σύνδεση",
      loggingIn: "Σύνδεση...",
      // New game options
      searchForOpponent: "Αναζήτηση αντιπάλου:",
      enterUsernameToSearch: "Εισάγετε όνομα χρήστη για αναζήτηση...",
      timeControl: "Έλεγχος Χρόνου:",
      selectTimeControl: "Επιλέξτε έλεγχο χρόνου",
      yourColor: "Το Χρώμα σας:",
      white: "Λευκό",
      black: "Μαύρο",
      random: "Τυχαίο",
      bullet: "Bullet",
      blitz: "Blitz",
      rapid: "Rapid",
      // Notifications
      viewInvitations: "Προβολή Προσκλήσεων",
      dismiss: "Απόρριψη",
      // Error messages
      pleaseEnterUsername: "Παρακαλώ εισάγετε το όνομα χρήστη ή email σας",
      pleaseEnterPassword: "Παρακαλώ εισάγετε τον κωδικό σας",
      passwordsDoNotMatch: "Οι κωδικοί δεν ταιριάζουν",
      usernameLength: "Το όνομα χρήστη πρέπει να είναι μεταξύ 3 και 50 χαρακτήρων",
      validEmail: "Παρακαλώ εισάγετε έγκυρο email",
      invalidCredentials: "Μη έγκυρο όνομα χρήστη ή κωδικός. Παρακαλώ ελέγξτε τα στοιχεία σας και δοκιμάστε ξανά.",
      accountDisabled: "Ο λογαριασμός σας έχει απενεργοποιηθεί. Παρακαλώ επικοινωνήστε με την υποστήριξη για βοήθεια.",
      accountLocked: "Ο λογαριασμός σας έχει κλειδωθεί. Παρακαλώ επικοινωνήστε με την υποστήριξη για βοήθεια.",
      authenticationFailed: "Η πιστοποίηση απέτυχε. Παρακαλώ ελέγξτε τα στοιχεία σας και δοκιμάστε ξανά.",
      errorDuringLogin: "Παρουσιάστηκε σφάλμα κατά τη σύνδεση. Παρακαλώ δοκιμάστε ξανά.",
      signupSuccessful: "Η εγγραφή ήταν επιτυχής αλλά η σύνδεση απέτυχε. Παρακαλώ συνδεθείτε χειροκίνητα.",
      accessDenied: "Απαγορεύεται η πρόσβαση. Παρακαλώ δοκιμάστε αργότερα.",
      invalidInput: "Μη έγκυρα δεδομένα. Παρακαλώ ελέγξτε τα στοιχεία σας και δοκιμάστε ξανά.",
      errorCreatingGame: "Σφάλμα δημιουργίας παιχνιδιού:",
      // Invitation notifications
      invitedYouToGame: "Σας προσκάλεσε σε παιχνίδι",
      minuteGame: "λεπτό παιχνίδι!",
      youWillPlayAs: "Θα παίξετε ως",
      color: "χρώμα",
      youWillBeRedirected: "Θα μεταβείτε στο παιχνίδι όταν γίνει αποδεκτή",
      // Notification format
      from: "Από",
      invitationToGame: "Πρόσκληση σε παιχνίδι",
      // Time control strings
      minute: "λεπτού",
      minutes: "λεπτών",
      second: "δευτερόλεπτο",
      seconds: "δευτερόλεπτα",
      // Color translations for Greek
      white: "άσπρα",
      black: "μαύρα",
      random: "τυχαίο",
      // Game results
      youWon: "Κερδίσατε",
      youLost: "Χάσατε",
      gameEndedInDraw: "Το παιχνίδι έληξε ισόπαλο",
      byResignation: "λόγω παραίτησης",
      byCheckmate: "λόγω ματ",
      byTimeout: "λόγω χρόνου",
      // Match success/error messages
      matchCreatedSuccessfully: "Το παιχνίδι δημιουργήθηκε επιτυχώς",
      matchJoinedSuccessfully: "Συμμετείχατε επιτυχώς στο παιχνίδι",
      matchCreationFailed: "Αποτυχία δημιουργίας παιχνιδιού",
      matchJoinFailed: "Αποτυχία συμμετοχής στο παιχνίδι",
      invitationSent: "Η πρόσκληση στάλθηκε επιτυχώς στον ",
      invitationAccepted: "Η πρόσκληση έγινε αποδεκτή",
      invitationDeclined: "Η πρόσκληση απορρίφθηκε",
      // Additional invitation strings
      accept: "Αποδοχή",
      decline: "Απόρριψη",
      // Redirect messages
      redirectingToGame: "Μετάβαση στο παιχνίδι...",
      // Invitation error messages
      cannotInviteYourself: "Δεν μπορείτε να προσκαλέσετε τον εαυτό σας σε παιχνίδι",
      alreadyHaveActiveGame: "Έχετε ήδη ένα ενεργό παιχνίδι",
      opponentHasActiveGame: "Ο αντίπαλος έχει ήδη ένα ενεργό παιχνίδι",
      pendingInvitationExists: "Έχετε ήδη μια εκκρεμή πρόσκληση με αυτόν τον χρήστη",
      invitationNotFound: "Η πρόσκληση δεν βρέθηκε",
      canOnlyRespondToYourInvitations: "Μπορείτε να απαντήσετε μόνο σε προσκλήσεις που σας απευθύνονται",
      invitationNoLongerPending: "Αυτή η πρόσκληση δεν είναι πλέον εκκρεμής",
      invitationExpired: "Αυτή η πρόσκληση έχει λήξει",
      invalidAction: "Μη έγκυρη ενέργεια. Χρησιμοποιήστε 'αποδοχή' ή 'απόρριψη'",
      canOnlyCancelYourInvitations: "Μπορείτε να ακυρώσετε μόνο τις προσκλήσεις που στείλατε",
      // Game error messages
      canOnlyResignWhenInProgress: "Μπορείτε να παραιτηθείτε μόνο όταν το παιχνίδι είναι σε εξέλιξη",
      canOnlyOfferDrawWhenInProgress: "Μπορείτε να προτείνετε ισοπαλία μόνο όταν το παιχνίδι είναι σε εξέλιξη",
      canOnlyOfferDrawOnYourTurn: "Μπορείτε να προτείνετε ισοπαλία μόνο στην σειρά σας",
      failedToResignGame: "Αποτυχία παραίτησης παιχνιδιού",
      failedToOfferDraw: "Αποτυχία πρότασης ισοπαλίας",
      failedToRespondToDrawOffer: "Αποτυχία απάντησης στην πρόταση ισοπαλίας",
      failedToOfferRematch: "Αποτυχία πρότασης επανάληψης",
      failedToRespondToRematchOffer: "Αποτυχία απάντησης στην πρόταση επανάληψης",
      // Game success messages
      rematchOfferSent: "Η πρόταση επανάληψης στάλθηκε στον αντίπαλό σας",
      drawOfferSent: "Η πρόταση ισοπαλίας στάλθηκε στον αντίπαλό σας",
      gameResignedSuccessfully: "Το παιχνίδι παραιτήθηκε επιτυχώς",
      rematchAccepted: "Η επανάληψη αποδέχτηκε",
      // Popup button translations
      successTitle: " ",
      okButton: "Εντάξει",
      // Login/Logout messages
      successfullyLoggedIn: "Συνδεθήκατε επιτυχώς",
      successfullyLoggedOut: "Έχετε αποσυνδεθεί επιτυχώς",
      // Game result messages
      youWon: "Κερδίσατε",
      youLost: "Χάσατε",
      whiteWins: "Τα λευκά κερδίσαν",
      blackWins: "Τα μαύρα κερδίσαν",
      gameEndedInDraw: "Το παιχνίδι έληξε ισόπαλο",
      draw: "Ισοπαλία",
      byResignation: "λόγω παραίτησης",
      byCheckmate: "λόγω ματ",
      byTimeout: "λόγω χρόνου",
      byStalemate: "λόγω πατ",
      byAgreement: "λόγω συμφωνίας",
      gameOver: "Τέλος Παιχνιδιού",
      checkmate: "Ματ",
      stalemate: "Πατ",
      timeout: "Λήξη Χρόνου",
      resignation: "Παραίτηση",
      // Game interface messages
      noMovesYet: "Δεν έχουν γίνει κινήσεις ακόμα",
      // Username change modal
      changeusernamelang: "Αλλαγή Ονόματος Χρήστη",
      newUsername: "Νέο Όνομα Χρήστη",
      save: "Αποθήκευση",
      cancel: "Ακύρωση",
      // Music toggle
      toggleMusic: "Εναλλαγή Μουσικής",
      musicOn: "Μουσική",
      musicOff: "Μουσική"
    }
  };
  
  // 2. Function to get translation by key
  function getTranslation(key) {
    const lang = localStorage.getItem('lang') || 'gr';
    return translations[lang][key] || key;
  }

  // 3. Function to translate backend error messages
  function translateErrorMessage(errorMessage) {
    // Map backend error messages to translation keys
    const errorMappings = {
      // Invitation errors
      "You cannot invite yourself to a game": "cannotInviteYourself",
      "You already have an active game": "alreadyHaveActiveGame", 
      "The opponent already has an active game": "opponentHasActiveGame",
      "You already have a pending invitation with this user": "pendingInvitationExists",
      "Invitation not found": "invitationNotFound",
      "You can only respond to invitations sent to you": "canOnlyRespondToYourInvitations",
      "This invitation is no longer pending": "invitationNoLongerPending",
      "This invitation has expired": "invitationExpired",
      "Invalid action. Use 'accept' or 'decline'": "invalidAction",
      "You can only cancel invitations you sent": "canOnlyCancelYourInvitations",
      // Game errors
      "You can only resign when the game is in progress": "canOnlyResignWhenInProgress",
      "You can only offer a draw when the game is in progress": "canOnlyOfferDrawWhenInProgress",
      "You can only offer a draw on your turn": "canOnlyOfferDrawOnYourTurn",
      "Failed to resign game": "failedToResignGame",
      "Failed to offer draw": "failedToOfferDraw",
      "Failed to respond to draw offer": "failedToRespondToDrawOffer",
      "Failed to offer rematch": "failedToOfferRematch",
      "Failed to respond to rematch offer": "failedToRespondToRematchOffer"
    };
    
    // Check if we have a translation for this error message
    const translationKey = errorMappings[errorMessage];
    if (translationKey) {
      return getTranslation(translationKey);
    }
    
    // If no translation found, return the original message
    return errorMessage;
  }

  // 3. Function to get color translation for Greek
  function getColorTranslation(color) {
    const lang = localStorage.getItem('lang') || 'gr';
    
    if (lang === 'gr') {
      // Greek: "Θα παίξετε με τα άσπρα/μαύρα"
      if (color === 'white') {
        return `Θα παίξετε με τα ${getTranslation('white')}`;
      } else if (color === 'black') {
        return `Θα παίξετε με τα ${getTranslation('black')}`;
      } else {
        return `Θα παίξετε με ${getTranslation('random')} χρώμα`;
      }
    } else {
      // English: "You will play as white/black color"
      return `${getTranslation('youWillPlayAs')} ${color} ${getTranslation('color')}`;
    }
  }

  // 4. Function to get time control translation
  function getTimeControlTranslation(timeControl) {
    const lang = localStorage.getItem('lang') || 'gr';
    
    // Check if it's seconds (contains 'sec' or is less than 1 minute)
    if (timeControl.toString().includes('sec') || timeControl < 1) {
      const seconds = Math.round(timeControl * 60); // Convert to seconds
      if (lang === 'gr') {
        // Greek: "30 δευτερόλεπτα" (no "παιχνίδι" at the end)
        if (seconds === 1) {
          return `${seconds} ${getTranslation('second')}`;
        } else {
          return `${seconds} ${getTranslation('seconds')}`;
        }
      } else {
        // English: "30 seconds game"
        if (seconds === 1) {
          return `${seconds} ${getTranslation('second')} game`;
        } else {
          return `${seconds} ${getTranslation('seconds')} game`;
        }
      }
    } else {
      // It's minutes
      const minutes = Math.round(timeControl);
      if (lang === 'gr') {
        // Greek: "5 λεπτών" (no "παιχνίδι" at the end)
        if (minutes === 1) {
          return `${minutes} ${getTranslation('minute')}`;
        } else {
          return `${minutes} ${getTranslation('minutes')}`;
        }
      } else {
        // English: "5 minutes game"
        if (minutes === 1) {
          return `${minutes} ${getTranslation('minute')} game`;
        } else {
          return `${minutes} ${getTranslation('minutes')} game`;
        }
      }
    }
  }

  // Function to update music toggle tooltip based on current state
  function updateMusicToggleTooltip() {
    const musicPlayer = document.querySelector('.music-player');
    if (musicPlayer) {
        const lang = localStorage.getItem('lang') || 'gr';
        const isMuted = musicPlayer.classList.contains('unmuted');
        const tooltipText = isMuted ? translations[lang].musicOn : translations[lang].musicOff;
        musicPlayer.setAttribute('data-tooltip', tooltipText);
        console.log('Updated music tooltip:', tooltipText, 'Language:', lang, 'Is muted:', isMuted);
        console.log('Current data-tooltip attribute:', musicPlayer.getAttribute('data-tooltip'));
        
        // Force a style update
        musicPlayer.style.setProperty('--tooltip-text', `"${tooltipText}"`);
    } else {
        console.log('Music player element not found');
    }
  }

  // Initialize tooltip when DOM is ready
  document.addEventListener('DOMContentLoaded', function() {
    // Wait a bit for the music player to be fully loaded
    setTimeout(() => {
      updateMusicToggleTooltip();
      
      // Add event listener to music player for state changes
      const musicPlayer = document.querySelector('.music-player');
      if (musicPlayer) {
        musicPlayer.addEventListener('click', function() {
          // Update tooltip after a short delay to allow state change
          setTimeout(() => {
            updateMusicToggleTooltip();
          }, 100);
        });
      }
    }, 100);
  });

  // Make the function globally available for testing
  window.updateMusicToggleTooltip = updateMusicToggleTooltip;
  
  // Also create a more direct update function
  window.forceUpdateMusicTooltip = function() {
    const musicPlayer = document.querySelector('.music-player');
    if (musicPlayer) {
      const lang = localStorage.getItem('lang') || 'gr';
      const isMuted = musicPlayer.classList.contains('unmuted');
      const tooltipText = isMuted ? translations[lang].musicOn : translations[lang].musicOff;
      
      // Force update the attribute
      musicPlayer.setAttribute('data-tooltip', tooltipText);
      
      // Also try to force a style recalculation
      musicPlayer.style.display = 'none';
      musicPlayer.offsetHeight; // Trigger reflow
      musicPlayer.style.display = '';
      
      console.log('Force updated music tooltip:', tooltipText);
    }
  };

  // 3. Function to update all translatable areas
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
    
    // Update username modal button texts
    const saveBtnModal = document.getElementById('saveusernamebtnmodal');
    if (saveBtnModal) saveBtnModal.textContent = translations[lang].save;
    const cancelBtnModal = document.getElementById('cancelusernamebtnmodal');
    if (cancelBtnModal) cancelBtnModal.textContent = translations[lang].cancel;
    
    // Update music toggle tooltip immediately when language changes
    setTimeout(() => {
        updateMusicToggleTooltip();
        // Also try the force update approach
        if (typeof window.forceUpdateMusicTooltip === 'function') {
          window.forceUpdateMusicTooltip();
        }
    }, 50);
    
    localStorage.setItem('lang', lang);
    document.querySelectorAll('.burger-lang').forEach(span => {
        span.classList.remove('active');
    });
    const activeSpan = document.getElementById('lang-' + lang);
    if (activeSpan) activeSpan.classList.add('active');
}
  
