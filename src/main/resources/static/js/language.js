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
      featuredescriptionlang :  "With the historic White Tower of 1919 and classical music in the background. A unique experience that combines chess with the history of Thessaloniki.",
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
      googlePrivacyNotice: "By signing in with Google, you agree to their privacy policy and cookie usage.",
      or: "or",
      emailOrUsername: "Email or username",
      emailOrUsernamePlaceholder: "Email or username",
      password: "Password",
      passwordPlaceholder: "Password",
      logIn: "Log In",
      dontHaveAccount: "Don't have an account?",
      signUp: "Sign up",
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
      musicOff: "Music",
      inProgressLang: "In Progress",
      // Newspaper translations
      newspaperCoreArchitecture: "Core Architecture",
      newspaperDeepDive: "Deep Dive",
      newspaperWelcomeToSKGChess: "Welcome to SKG Chess",
      newspaperJustTheBeginning: "Just the beginning",
      newspaperCustomChessEngine: "Custom Chess Engine!",
      newspaperByHaris: "by Haris Chatzianastasiou",
      newspaperCustomEngineDescription: "Unlike most chess projects that rely on existing libraries, SKG Chess implements its own chess engine from scratch in Java. This gives us complete control over the game logic and allows for deep optimization. Let's see how this works!",
      newspaperHeartOfEngine: "The heart of the engine",
      newspaperHeartDescription: "At the heart of the engine lies the Board class, which follows an immutable design pattern. The important thing to understand is that each time a move is played, a new Board is created, hoding the information of the tiles (empty or occupied -- which piece is on it -- ) and the players (current and opponent, with their pieces and list of moves they can play). Here's the Board class describing the structure of the board:",
      newspaperPiecesAndMoves: "Pieces and Moves",
      newspaperPiecesDescription: "Each player has a collection of pieces and a collections of moves they can make.",
      newspaperPiecesDescription2: "Let's see how each piece on the board is represented by a Piece object.",
      newspaperMoveDescription: "Let's also see how we represent a move with the Move class.",
      newspaperHowDoesItWork: "But how does it work?",
      newspaperTechnicalWarning: "Warning : Technical content ahead! If you thought the left section was complex, buckle up - we're about to dive into the chess engine's brain! 🧠♟️",
      newspaperStep1: "Step 1 : Game Initialization",
      newspaperStep1Description: "When a new game starts, a new Game entity is created in the database. The initial board state is created using the Builder pattern through the createStandardBoard() method:",
      newspaperStep2: "Step 2 : User clicks a piece from position x to position y",
      newspaperStep2Description: "When a player clicks a piece, the following sequence occurs:",
      newspaperStep2List1: "Frontend captures the tile coordinate of the clicked piece",
      newspaperStep2List2: "Frontend sends a POST request to /api/game/{gameId}/move with:",
      newspaperStep2List2a: "sourceCoordinate: The tile coordinate of the clicked piece",
      newspaperStep2List2b: "targetCoordinate: The tile coordinate where the piece will move",
      newspaperStep3: "Step 3 : The move is processed",
      newspaperStep3Description: "The move is processed through several steps:",
      newspaperStep3List1: "GameController receives the move request and calls GameService",
      newspaperStep3List2: "GameService retrieves the current game state from the database",
      newspaperStep3List3: "The board is deserialized from the stored JSON",
      newspaperStep3List4: "The current player's legal moves are retrieved",
      newspaperStep4: "Step 4 : Board Creation & Player Updates",
      newspaperStep4Description: "If the move played is in the list of legal moves, a new Board is created with updated tiles and players.",
      newspaperStep4ForEach: "For each player, the following happens:",
      newspaperStep4List1: "All pieces of the player's color are collected from the tiles",
      newspaperStep4List2: "Legal moves are calculated for each piece through a complex process:",
      newspaperStep4List2a: "For each piece, calculate all possible destination coordinates based on its movement pattern:",
      newspaperStep4List2b: "Apply multiple validation layers for each potential move:",
      newspaperStep4List2c: "For sliding pieces (Bishop, Rook, Queen), continue checking in each direction until:",
      newspaperStep4List2d: "For fixed-distance pieces (Pawn, Knight, King):",
      newspaperStep4List3: "The player is created with:",
      newspaperStep5: "Step 5 : Game Update",
      newspaperStep5Description: "After the move is executed:",
      newspaperStep5List1: "The new board state is serialized to JSON",
      newspaperStep5List2: "The game entity is updated in the database",
      newspaperStep5List3: "The new state is broadcast to both players via WebSocket",
      newspaperStep5List4: "The frontend updates the board display",
      newspaperSpringBootIntegration: "Spring Boot Integration",
      newspaperSpringBootSubtitle: "Because We're Not Savages - We Use Spring Boot! 🌱",
      newspaperSpringBootDescription: "Ah yes, Spring Boot! The framework that makes Java developers feel like they're not living in the stone age. SKG Chess proudly rides the Spring Boot train, because who doesn't love auto-configuration? 🚂",
      newspaperFancyLayers: "Our Fancy Application Layers",
      newspaperTechStack: "Our Tech Stack (AKA The Cool Kids' Table)",
      newspaperWebSocket: "WebSocket - For those sweet, sweet real-time updates (no carrier pigeons here!) 📡",
      newspaperPostgreSQL: "PostgreSQL - Our reliable data butler, always ready to serve 🎩",
      newspaperThymeleaf: "Thymeleaf - Our trusty template engine for server-side rendering 🎨",
      // Additional bullet point translations
      newspaperBasicValidations: "Basic validations (bounds, alliance, pattern)",
      newspaperCheckValidations: "Check validations (king safety, pins)",
      newspaperSpecialMoveValidations: "Special move validations (castling, en passant)",
      newspaperHittingBoardEdge: "Hitting the board edge",
      newspaperHittingPiece: "Hitting a piece (stop or capture)",
      newspaperFailingValidation: "Failing a validation",
      newspaperCheckDestination: "Check each possible destination once",
      newspaperValidateSpecial: "Validate special conditions (first move, castling rights)",
      newspaperCollectionPieces: "Collection of pieces",
      newspaperCollectionMoves: "Collection of legal moves",
      newspaperPlayerAlliance: "Player's alliance (WHITE/BLACK)"
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
      featuredescriptionlang : "Με φόντο τον λευκό πύργο του 1919 και διάσημα έργα της κλασικής μουσικής. Μια ξεχωριστή εμπειρία που συνδυάζει το σκάκι με την ιστορία της Θεσσαλονίκης.",
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
      googlePrivacyNotice: "Συνδέοντας με το Google, συμφωνείτε με την πολιτική απορρήτου και τη χρήση cookies του Google.",
      or: "ή",
      emailOrUsername: "Email ή όνομα χρήστη",
      emailOrUsernamePlaceholder: "Email ή όνομα χρήστη",
      password: "Κωδικός",
      passwordPlaceholder: "Κωδικός",
      rememberMe: "Θυμήσου με",
      logIn: "Σύνδεση",
      dontHaveAccount: "Δεν έχετε λογαριασμό;",
      signUp: "Εγγραφή",
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
      musicOff: "Μουσική",
      inProgressLang: "Σε εξέλιξη",
      // Newspaper translations
      newspaperCoreArchitecture: "Ανάλυση",
      newspaperDeepDive: "Βασικής Αρχιτεκτονικής",
      newspaperWelcomeToSKGChess: "Καλώς ήρθατε στο SKG Chess",
      newspaperJustTheBeginning: "Μόνο η αρχή",
      newspaperCustomChessEngine: "Προσαρμοσμένη Μηχανή Σκακιού!",
      newspaperByHaris: "από τον Χάρη Χατζηαναστασίου",
      newspaperCustomEngineDescription: "Σε αντίθεση με τα περισσότερα παιχνίδια που βασίζονται σε υπάρχουσες βιβλιοθήκες, το SKG Chess υλοποιεί τη δική του μηχανή σκακιού σε γλώσσα προγραμματισμού Java. Ας δούμε πώς λειτουργεί!",
      newspaperHeartOfEngine: "Η καρδιά της μηχανής",
      newspaperHeartDescription: "Στην καρδιά της μηχανής βρίσκεται η κλάση Board, που ακολουθεί immutable design pattern. Το σημαντικό είναι να καταλάβουμε ότι κάθε φορά που παίζεται μια κίνηση, δημιουργείται ένα νέο Board, που κρατάει τις πληροφορίες των τετραγώνων (άδεια ή κατειλημμένα, ποιο κομμάτι είναι πάνω τους) και των παικτών (τρέχων και αντίπαλος, με τα κομμάτια τους και τη λίστα κινήσεων που μπορούν να παίξουν). Εδώ είναι η κλάση Board που περιγράφει τη δομή της σκακιέρας :",
      newspaperPiecesAndMoves: "Κομμάτια και Κινήσεις",
      newspaperPiecesDescription: "Κάθε παίκτης έχει μια συλλογή κομματιών και μια συλλογή κινήσεων που μπορεί να κάνει.",
      newspaperPiecesDescription2: "Ας δούμε πώς κάθε κομμάτι στη σκακιέρα αντιπροσωπεύεται στη Java από ένα αντικείμενο Piece.",
      newspaperMoveDescription: "Ας δούμε επίσης πώς αντιπροσωπεύουμε μια κίνηση με την κλάση Move.",
      newspaperHowDoesItWork: "Πως λειτουργεί ;",
      newspaperTechnicalWarning: " ",
      newspaperStep1: "Βήμα 1: Αρχικοποίηση Παιχνιδιού",
      newspaperStep1Description: "Όταν ξεκινά ένα νέο παιχνίδι, δημιουργείται μια νέα οντότητα Game στη βάση δεδομένων. Η αρχική κατάσταση της σκακιέρας δημιουργείται χρησιμοποιώντας το Builder pattern μέσω της μεθόδου createStandardBoard():",
      newspaperStep2: "Βήμα 2: Ο χρήστης κάνει κλικ σε ένα κομμάτι από τη θέση x στη θέση y",
      newspaperStep2Description: "Όταν ένας παίκτης κάνει κλικ σε ένα κομμάτι, συμβαίνει η ακόλουθη διαδικασία:",
      newspaperStep2List1: "Το frontend καταγράφει τη συντεταγμένη του τετραγώνου του κομματιού",
      newspaperStep2List2: "Το frontend στέλνει ένα POST request στο /api/game/{gameId}/move με:",
      newspaperStep2List2a: "sourceCoordinate: Η συντεταγμένη του τετραγώνου του κομματιού που επιλέχθηκε.",
      newspaperStep2List2b: "targetCoordinate: Η συντεταγμένη του τετραγώνου όπου θα μετακινηθεί το κομμάτι.",
      newspaperStep3: "Βήμα 3: Η κίνηση επεξεργάζεται",
      newspaperStep3Description: "Η κίνηση επεξεργάζεται μέσω πολλών βημάτων:",
      newspaperStep3List1: "Το GameController λαμβάνει το αίτημα κίνησης και καλεί το GameService",
      newspaperStep3List2: "Το GameService ανακτά την τρέχουσα κατάσταση του παιχνιδιού από τη βάση δεδομένων",
      newspaperStep3List3: "Η σκακιέρα κατασκευάζεται από το αποθηκευμένο JSON",
      newspaperStep3List4: "Ανακτώνται οι νόμιμες κινήσεις του τρέχοντος παίκτη",
      newspaperStep4: "Βήμα 4: Δημιουργία Σκακιέρας & Ενημερώσεις Παικτών",
      newspaperStep4Description: "Αν η παιχθείσα κίνηση είναι στη λίστα των νόμιμων κινήσεων, δημιουργείται ένα νέο Board με ενημερωμένα τετράγωνα και παίκτες.",
      newspaperStep4ForEach: "Για κάθε παίκτη, συμβαίνει το ακόλουθο:",
      newspaperStep4List1: "Όλα τα κομμάτια του χρώματος του παίκτη συλλέγονται από τα τετράγωνα",
      newspaperStep4List2: "Οι νόμιμες κινήσεις υπολογίζονται για κάθε κομμάτι μέσω μιας περίπλοκης διαδικασίας:",
      newspaperStep4List2a: "Για κάθε κομμάτι, υπολογίζονται όλες οι πιθανές συντεταγμένες προορισμού, βάσει του μοτίβου κίνησής του:",
      newspaperStep4List2b: "Εφαρμόζονται πολλαπλά στρώματα επαλήθευσης για κάθε πιθανή κίνηση:",
      newspaperStep4List2c: "Για τα κομμάτια ολίσθησης (Αξιωματικός, Πύργος, Βασίλισσα) συνεχίζεται ο έλεγχος σε κάθε κατεύθυνση μέχρι να συμβεί ένα από τα ακόλουθα :",
      newspaperStep4List2d: "Για τα κομμάτια σταθερής απόστασης (Πιόνι, Ίππος, Βασιλιάς):",
      newspaperStep4List3: "Ο παίκτης δημιουργείται με:",
      newspaperStep5: "Βήμα 5: Ενημέρωση Παιχνιδιού",
      newspaperStep5Description: "Μετά την εκτέλεση της κίνησης:",
      newspaperStep5List1: "Η νέα κατάσταση της σκακιέρας σειριοποιείται σε JSON",
      newspaperStep5List2: "Η οντότητα του παιχνιδιού ενημερώνεται στη βάση δεδομένων",
      newspaperStep5List3: "Η νέα κατάσταση μεταδίδεται και στους δύο παίκτες μέσω WebSocket",
      newspaperStep5List4: "Το frontend ενημερώνει την εμφάνιση της σκακιέρας",
      newspaperSpringBootIntegration: "Ενσωμάτωση Spring Boot",
      newspaperSpringBootSubtitle: "🌱",
      newspaperSpringBootDescription: " ",
      newspaperFancyLayers: "Τα στρώματα της εφαρμογής μας",
      newspaperTechStack: "Τech Stack",
      newspaperWebSocket: "WebSocket - Για αυτές τις γλυκές, γλυκές real-time ενημερώσεις (όχι αγριοπερίστερα εδώ!) 📡",
      newspaperPostgreSQL: "PostgreSQL - Ο αξιόπιστος μπάτλερ δεδομένων μας, πάντα έτοιμος να εξυπηρετήσει 🎩",
      newspaperThymeleaf: "Thymeleaf - Η αξιόπιστη μηχανή προτύπων μας για server-side rendering 🎨",
      // Additional bullet point translations
      newspaperBasicValidations: "Βασικές επαληθεύσεις (όρια, χρώμα τετραγώνου, μοτίβο)",
      newspaperCheckValidations: "Επαληθεύσεις check (ασφάλεια βασιλιά, pins)",
      newspaperSpecialMoveValidations: "Επαληθεύσεις ειδικών κινήσεων (ροκέ, en passant)",
      newspaperHittingBoardEdge: "Χτύπημα στο άκρο της σκακιέρας",
      newspaperHittingPiece: "Χτύπημα σε κομμάτι",
      newspaperFailingValidation: "Αποτυχία επαλήθευσης",
      newspaperCheckDestination: "Έλεγχος κάθε πιθανού προορισμό μία φορά",
      newspaperValidateSpecial: "Επαλήθευση ειδικών συνθηκών (πρώτη κίνηση, δικαιώματα ροκέ)",
      newspaperCollectionPieces: "Συλλογή κομματιών",
      newspaperCollectionMoves: "Συλλογή νόμιμων κινήσεων",
      newspaperPlayerAlliance: "Χρώμα παίκτη (ΛΕΥΚΑ/ΜΑΥΡΑ)"
    }
  };
  
  // 2. Function to get translation by key
  function getTranslation(key) {
    const lang = getCurrentLanguage();
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
        
        // Force a style update
        musicPlayer.style.setProperty('--tooltip-text', `"${tooltipText}"`);
    } else {
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
      
    }
  };

  // Function to get current language from URL or localStorage
  function getCurrentLanguage() {
    // First, try to get language from URL path
    const pathSegments = window.location.pathname.split('/').filter(segment => segment !== '');
    if (pathSegments.length > 0 && (pathSegments[0] === 'en' || pathSegments[0] === 'gr')) {
      return pathSegments[0];
    }
    
    // Fallback to localStorage
    return localStorage.getItem('lang') || 'gr';
  }

  // Function to update URL with language parameter
  function updateUrlWithLanguage(lang) {
    const currentPath = window.location.pathname;
    const pathSegments = currentPath.split('/').filter(segment => segment !== '');
    
    // Remove existing language from path if present
    if (pathSegments.length > 0 && (pathSegments[0] === 'en' || pathSegments[0] === 'gr')) {
      pathSegments.shift();
    }
    
    // Add new language to the beginning
    pathSegments.unshift(lang);
    
    // Reconstruct the URL
    const newPath = '/' + pathSegments.join('/');
    const newUrl = window.location.origin + newPath + window.location.search + window.location.hash;
    
    // Update URL without page reload
    window.history.pushState({}, '', newUrl);
  }

  // Function to update logo link with current language
  function updateLogoLink() {
    const logoLink = document.getElementById('logo-link');
    if (logoLink) {
      const currentLang = getCurrentLanguage();
      logoLink.href = '/' + currentLang;
    }
  }

  // 3. Function to update all translatable areas
  function updateLanguage(lang) {
    // Update URL with language parameter
    updateUrlWithLanguage(lang);
    
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
    
    // Update logo link with current language
    updateLogoLink();
    
    // Update music toggle tooltip immediately when language changes
    setTimeout(() => {
        updateMusicToggleTooltip();
        // Also try the force update approach
        if (typeof window.forceUpdateMusicTooltip === 'function') {
          window.forceUpdateMusicTooltip();
        }
        // Update quotes when language changes
        if (typeof window.updateQuotesOnLanguageChange === 'function') {
          window.updateQuotesOnLanguageChange();
        }
        // Update newspaper content when language changes
        if (typeof window.updateNewspaperContent === 'function') {
          window.updateNewspaperContent();
        }
    }, 50);
    
    localStorage.setItem('lang', lang);
    document.querySelectorAll('.burger-lang').forEach(span => {
        span.classList.remove('active');
    });
    const activeSpan = document.getElementById('lang-' + lang);
    if (activeSpan) activeSpan.classList.add('active');
}

// Function to update newspaper content when language changes
function updateNewspaperContent() {
  const lang = getCurrentLanguage();
  const newspaperElements = [
    'newspaperCoreArchitecture', 'newspaperDeepDive', 'newspaperWelcomeToSKGChess', 
    'newspaperJustTheBeginning', 'newspaperCustomChessEngine', 'newspaperByHaris',
    'newspaperCustomEngineDescription', 'newspaperHeartOfEngine', 'newspaperHeartDescription',
    'newspaperPiecesAndMoves', 'newspaperPiecesDescription', 'newspaperPiecesDescription2',
    'newspaperMoveDescription', 'newspaperHowDoesItWork', 'newspaperTechnicalWarning',
    'newspaperStep1', 'newspaperStep1Description', 'newspaperStep2', 'newspaperStep2Description',
    'newspaperStep2List1', 'newspaperStep2List2', 'newspaperStep2List2a', 'newspaperStep2List2b',
    'newspaperStep3', 'newspaperStep3Description', 'newspaperStep3List1', 'newspaperStep3List2',
    'newspaperStep3List3', 'newspaperStep3List4', 'newspaperStep4', 'newspaperStep4Description',
    'newspaperStep4ForEach', 'newspaperStep4List1', 'newspaperStep4List2', 'newspaperStep4List2a',
    'newspaperStep4List2b', 'newspaperStep4List2c', 'newspaperStep4List2d', 'newspaperStep4List3',
    'newspaperStep5', 'newspaperStep5Description', 'newspaperStep5List1', 'newspaperStep5List2',
    'newspaperStep5List3', 'newspaperStep5List4', 'newspaperSpringBootIntegration',
    'newspaperSpringBootSubtitle', 'newspaperSpringBootDescription', 'newspaperFancyLayers',
    'newspaperTechStack', 'newspaperWebSocket', 'newspaperPostgreSQL', 'newspaperThymeleaf',
    'newspaperBasicValidations', 'newspaperCheckValidations', 'newspaperSpecialMoveValidations',
    'newspaperHittingBoardEdge', 'newspaperHittingPiece', 'newspaperFailingValidation',
    'newspaperCheckDestination', 'newspaperValidateSpecial', 'newspaperCollectionPieces',
    'newspaperCollectionMoves', 'newspaperPlayerAlliance'
  ];
  
  newspaperElements.forEach(elementId => {
    const element = document.getElementById(elementId);
    if (element && translations[lang] && translations[lang][elementId]) {
      element.textContent = translations[lang][elementId];
    }
  });
}

// Make the function globally available
window.updateNewspaperContent = updateNewspaperContent;
  
