class ChessGame {
    constructor() {
        this.statusElement = document.getElementById('status');
        if (!this.statusElement) {
            this.statusElement = document.createElement('div');
            this.statusElement.id = 'status';
            document.querySelector('.game-section').appendChild(this.statusElement);
        }
        this.board = document.getElementById('game-board');
        this.piece = document.getElementById('piece');
        this.boardDTO = null;
        this.gameId = null;
        this.gameStatus = null;
        this.boardOrientation = null;
        this.playerColor = null;
        this.isPlayerTurn = false;
        this.lastMoveArrow = null;
        
        // Timer-related properties
        this.whiteTimeLeftSeconds = null;
        this.blackTimeLeftSeconds = null;
        this.lastMoveAt = null;
        this.timeControlMinutes = null;
        this.timerInterval = null;
        this.serverTimeOffset = 0; // Time offset between client and server (in milliseconds)
        this.timeoutDetected = false; // Flag to track if timeout has been detected
        this.isUpdatingFromServer = false; // Flag to prevent timer updates during server sync
        
        // Position navigation properties
        this.isViewingMode = false; // Track if we're in viewing mode (browsing positions)
        this.currentPositionIndex = -1; // Current position index (-1 = latest position)
        this.gamePositions = []; // Array of all game positions
        this.latestPositionBoardDTO = null; // Store the latest position for comparison
        
        // Navigation button elements
        this.prevPositionBtn = document.getElementById('prev-position-btn');
        this.nextPositionBtn = document.getElementById('next-position-btn');
        this.initialPositionBtn = document.getElementById('initial-position-btn');
        this.latestPositionBtn = document.getElementById('latest-position-btn');
        
        this.pieceImages = {
            'WHITE_PAWN': '/images/pawnN.png',
            'WHITE_KNIGHT': '/images/knightN.png',
            'WHITE_BISHOP': '/images/bishopN.png',
            'WHITE_ROOK': '/images/rookN.png',
            'WHITE_QUEEN': '/images/queenN.png',
            'WHITE_KING': '/images/kingN.png',
            'BLACK_PAWN': '/images/pawnN1.png',
            'BLACK_KNIGHT': '/images/knightN1.png',
            'BLACK_BISHOP': '/images/bishopN1.png',
            'BLACK_ROOK': '/images/rookN1.png',
            'BLACK_QUEEN': '/images/queenN1.png',
            'BLACK_KING': '/images/kingN1.png'
        };

        // this.initializeBoard();
        this.setupEventListeners();
        
        // Initialize gameId from URL
        this.gameId = window.location.pathname.split('/')[window.location.pathname.split('/').length - 1];
        
        this.username = document.getElementById('username').textContent; // Get the username from the HTML
        this.userId = null; // Will be set after fetching
        this.boardOrientation = null;
        this.stompClient = null;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        
        // Clear all visual indicators
        document.querySelectorAll('.last-move-source, .last-move-target').forEach(tile => {
            tile.classList.remove('last-move-source', 'last-move-target');
        });
        document.querySelector('.selected')?.classList.remove('selected');
        this.clearLegalMoves();
        
        // Remove the last move arrow if it exists
        if (this.lastMoveArrow) {
            this.lastMoveArrow.remove();
        }
        
        // Reset all state
        this.selectedSourceTile = null;
        
        // Initialize chat elements
        this.chatMessages = document.getElementById('chat-messages');
        this.chatInput = document.getElementById('chat-input');
        this.sendMessageBtn = document.getElementById('send-message');
        
        // Initialize enlarged chat modal elements
        this.chatModal = document.getElementById('chatModal');
        this.chatModalMessages = document.getElementById('chat-modal-messages');
        this.chatModalInput = document.getElementById('chat-modal-input');
        this.chatModalSendBtn = document.getElementById('chat-modal-send');
        this.chatModalCloseBtn = document.getElementById('chatModalClose');
        
        // Bind chat event handlers
        this.chatInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                this.sendChatMessage();
            }
        });
        this.sendMessageBtn.addEventListener('click', (e) => {
            e.stopPropagation(); // Prevent modal from opening
            this.sendChatMessage();
        });
        
        // Prevent chat input from triggering modal
        this.chatInput.addEventListener('click', (e) => {
            e.stopPropagation(); // Prevent modal from opening
        });
        
        // Bind enlarged chat modal event handlers
        this.chatModalInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                this.sendChatMessageFromModal();
            }
        });
        this.chatModalSendBtn.addEventListener('click', () => this.sendChatMessageFromModal());
        
        // Chat modal open/close handlers
        this.chatModalCloseBtn.addEventListener('click', (e) => {
            e.stopPropagation(); // Prevent event bubbling
            this.toggleChatModal();
        });
        
        // Make chat section clickable to toggle modal
        const chatSection = document.querySelector('.game-chat-section');
        if (chatSection) {
            chatSection.addEventListener('click', (e) => {
                // Don't toggle modal if clicking on input or button
                if (e.target.closest('.chat-input') || e.target.closest('input') || e.target.closest('button')) {
                    return;
                }
                this.toggleChatModal();
            });
        }
        
        // Close modal on escape key
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && this.chatModal.classList.contains('show')) {
                this.toggleChatModal();
            }
        });
        
        // Initialize the game asynchronously
        this.initializeGame();
        
        // Add sound effects
        this.sounds = {
            move: new Audio('/audio/Move.wav'),
            capture: new Audio('/audio/Capture.wav'),
            check: new Audio('/audio/Check.wav')
        };
        
        // Preload sounds with comprehensive debugging
        Object.entries(this.sounds).forEach(([type, sound]) => {
            // Add error handler first
            sound.addEventListener('error', (e) => {
                // Silent error handling for speed
            });
            
            // Add success handlers
            sound.addEventListener('loadeddata', () => {
                // Silent success handling for speed
            });
            
            sound.addEventListener('canplaythrough', () => {
                // Silent success handling for speed
            });
            
            // Try to load the sound
            try {
                sound.load();
            } catch (e) {
                // Silent error handling for speed
            }
        });

        // Game end popup tracking
        this.gameEndPopupShown = false;
        this.lastGameStatus = null;
        
        // Latest game state (for timer calculations when in viewing mode)
        this.latestGameState = null;
        
        // UI elements
        this.chatContainer = document.getElementById('chat-container');
        
        // Drag and drop state
        this.isDragging = false;
        this.draggedPiece = null;
        this.dragImage = null;
        this.lastHoveredTile = null; // For optimized hover tracking
        
        // Timer
        this.timerInterval = null;
        
        // WebSocket
        this.subscription = null;
        
        // Position navigation
        this.latestPositionBoardDTO = null;
        
        // Initialize the game
        this.initializeGame();
    }
    
    // Initialize the game asynchronously
    async initializeGame() {
        // Fetch user ID
        this.userId = await this.fetchUserIdByUsername(this.username);
        
        // Connect to WebSocket after user ID is fetched
        this.connectWebSocket();
        
        // Fetch initial game state
        await this.fetchGame();
        
        // Load game positions for analysis
        await this.loadGamePositions();

        // Remove the redundant animation trigger for black player
        // The animation will now only show when receiving the GAME_STARTED websocket message
    }

    // Start the timer countdown
    startTimer() {
        // Only start timer if game is in progress or in check (allow timeout detection)
        if (this.gameStatus !== 'IN_PROGRESS' && this.gameStatus !== 'CHECK') {
            return;
        }
        
        // Don't start if timer is already running
        if (this.timerInterval) {
            return;
        }
        
        // Don't start if we're updating from server
        if (this.isUpdatingFromServer) {
            return;
        }
        
        this.timerInterval = setInterval(() => {
            this.updateTimerDisplay();
        }, 10); // Update every 10ms for smooth millisecond display
    }

    // Stop the timer
    stopTimer() {
        if (this.timerInterval) {
            clearInterval(this.timerInterval);
            this.timerInterval = null;
        }
    }

    // Update timer display
    updateTimerDisplay() {
        // Don't update timer if game has ended (but allow CHECK status for timeout detection)
        if (this.gameStatus !== 'IN_PROGRESS' && this.gameStatus !== 'CHECK') {
            return;
        }
        
        // Don't update timer if we're currently syncing with server
        if (this.isUpdatingFromServer) {
            return;
        }
        
        // Don't update if we don't have valid timer data from server
        if (!this.lastMoveAt || !this.whiteTimeLeftSeconds || !this.blackTimeLeftSeconds) {
            return;
        }

        // Use server-synchronized time for accurate calculations
        const now = new Date();
        const serverAdjustedNow = new Date(now.getTime() + (this.serverTimeOffset || 0)); // Apply server time offset
        const lastMoveTime = new Date(this.lastMoveAt);
        const elapsedSeconds = (serverAdjustedNow - lastMoveTime) / 1000; // Use decimal seconds for millisecond precision

        // Calculate current time left for each player
        let whiteTimeLeft = this.whiteTimeLeftSeconds;
        let blackTimeLeft = this.blackTimeLeftSeconds;

        // Use latest game state for timer calculations (not historical position data)
        const currentPlayerAlliance = this.latestGameState?.currentPlayer?.alliance || this.boardDTO?.currentPlayer?.alliance;
            
        // Only decrease time for the player whose turn it is currently
        if (currentPlayerAlliance) {
            if (currentPlayerAlliance === 'WHITE') {
                // White's turn - decrease white's time
                whiteTimeLeft = Math.max(0, this.whiteTimeLeftSeconds - elapsedSeconds);
                // Black's time stays the same
                blackTimeLeft = this.blackTimeLeftSeconds;
            } else {
                // Black's turn - decrease black's time
                blackTimeLeft = Math.max(0, this.blackTimeLeftSeconds - elapsedSeconds);
                // White's time stays the same
                whiteTimeLeft = this.whiteTimeLeftSeconds;
            }
        }

        // Update display using existing timer elements
        const timerElements = document.querySelectorAll('.player-timer');
        if (timerElements.length >= 2) {
            // First timer element is opponent's timer (top)
            const opponentTimer = timerElements[0];
            const currentPlayerTimer = timerElements[1];
            
            // Determine which timer shows which player based on current player's color
            if (this.playerColor === 'WHITE') {
                // Current player is white, so opponent is black
                opponentTimer.innerHTML = this.formatTime(blackTimeLeft);
                currentPlayerTimer.innerHTML = this.formatTime(whiteTimeLeft);
                
                // Add low time warning
                opponentTimer.className = blackTimeLeft <= 10.0 ? 'player-timer low-time' : 'player-timer';
                currentPlayerTimer.className = whiteTimeLeft <= 10.0 ? 'player-timer low-time' : 'player-timer';
            } else {
                // Current player is black, so opponent is white
                opponentTimer.innerHTML = this.formatTime(whiteTimeLeft);
                currentPlayerTimer.innerHTML = this.formatTime(blackTimeLeft);
                
                // Add low time warning
                opponentTimer.className = whiteTimeLeft <= 10.0 ? 'player-timer low-time' : 'player-timer';
                currentPlayerTimer.className = blackTimeLeft <= 10.0 ? 'player-timer low-time' : 'player-timer';
            }
        }

        // Check for timeout - only check the player whose turn it is
        // Allow timeout detection even when game is in CHECK status
        if (currentPlayerAlliance) {
            if ((currentPlayerAlliance === 'WHITE' && whiteTimeLeft <= 0.001) || 
                (currentPlayerAlliance === 'BLACK' && blackTimeLeft <= 0.001)) {
                this.timeoutDetected = true; // Set flag to immediately disable interactions
                this.handleTimeout();
            }
        }
    }

    // Format time as MM:SS.mm (with 2-digit milliseconds)
    formatTime(seconds) {
        const minutes = Math.floor(seconds / 60);
        const remainingSeconds = Math.floor(seconds % 60);
        const milliseconds = Math.floor((seconds % 1) * 100); // Get 2 digits (centiseconds)
        return `${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}<span class="milliseconds">.${milliseconds.toString().padStart(2, '0')}</span>`;
    }

    // Handle timeout
    async handleTimeout() {
        this.stopTimer();
        
        try {
            const response = await fetch(`/api/games/${this.gameId}/timeout`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            });
            
            if (response.ok) {
                // Fetch the updated game state to get the new status and winner
                await this.fetchGame();
                
                // Update the board to show the end game popup
                this.updateBoard();
            }
        } catch (error) {
            // Silent error handling for speed
        }
    }

    // Start the game timer when inviter is redirected to the game page
    async startGameTimer() {
        try {
            const response = await fetch(`/api/games/${this.gameId}/start-timer`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            });
            
            if (response.ok) {
                // Fetch the updated game state to get the timer data
                await this.fetchGame();
            }
        } catch (error) {
            // Silent error handling for speed
        }
    }

    initializeBoard() {
        this.board.innerHTML = '';
        
        // Define files and ranks based on player color
        let files, ranks;
        
        // Base arrays for files and ranks
        if (this.playerColor === 'WHITE') {
             files = ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'];
             ranks = ['8', '7', '6', '5', '4', '3', '2', '1'];
        } else if (this.playerColor === 'BLACK') {
            // For black's perspective, we want to show the coordinates reversed but keep the internal positions the same
            files = ['h', 'g', 'f', 'e', 'd', 'c', 'b', 'a'];
            ranks = ['1', '2', '3', '4', '5', '6', '7', '8'];
        }
        
        for (let row = 0; row < 8; row++) {
            for (let col = 0; col < 8; col++) {
                const tile = document.createElement('div');
                const isDark = (row + col) % 2 !== 0;
                
                // Base tile class
                tile.className = `tile ${isDark ? 'dark' : 'light'}`;
                
                // If it's a dark tile, randomly assign one of two variants
                if (isDark) {
                    const variant = Math.floor(Math.random() * 2) + 1;
                    tile.classList.add(`dark-variant-${variant}`);
                }
                
                tile.dataset.position = row * 8 + col;
                tile.dataset.file = files[col];
                tile.dataset.rank = ranks[row];
                
                // Add file letter (a-h) with correct perspective
                const fileLabel = document.createElement('div');
                fileLabel.className = 'coordinate-file';
                fileLabel.textContent = this.playerColor === 'BLACK' ? 
                    files[7 - col] :  // Reverse for black's view
                    files[col];       // Normal for white's view
                tile.appendChild(fileLabel);
                
                // Add rank number (1-8) with correct perspective
                const rankLabel = document.createElement('div');
                rankLabel.className = 'coordinate-rank';
                rankLabel.textContent = this.playerColor === 'BLACK' ? 
                    ranks[7 - row] :  // Reverse for black's view
                    ranks[row];       // Normal for white's view
                tile.appendChild(rankLabel);
                
                this.board.appendChild(tile);
            }
        }
    }

    setupEventListeners() {
        this.board.addEventListener('click', (e) => this.handleTileClick(e));
        
        // Mouse event listeners for dragging
        this.board.addEventListener('mousedown', this.handleMouseDown.bind(this));
        document.addEventListener('mousemove', this.handleMouseMove.bind(this));
        document.addEventListener('mouseup', this.handleMouseUp.bind(this));

        // Add click event listener to clear selections when clicking outside the board
        document.addEventListener('click', (event) => {
            // Check if the click is outside the game board
            if (!event.target.closest('#game-board')) {
                // Clear selection and highlights
                document.querySelector('.selected')?.classList.remove('selected');
                this.selectedSourceTile = null;
                this.clearLegalMoves();
            }
        });

        this.board.addEventListener('contextmenu', (event) => {
            event.preventDefault();
            if (this.selectedSourceTile !== null) {
                this.clearLegalMoves();
                document.querySelector('.selected')?.classList.remove('selected');
                this.selectedSourceTile = null;
            }
        });
        
        // Setup position navigation event listeners
        this.setupPositionNavigationListeners();
        
        // Setup resign and draw button event listeners
        this.setupGameControlListeners();
    }

    /**
     * Setup event listeners for game control buttons (resign, draw)
     * This method adds click handlers for the resign and draw buttons
     */
    setupGameControlListeners() {
        // Get the resign and draw buttons
        const resignBtn = document.getElementById('resign-btn');
        const drawBtn = document.getElementById('draw-btn');
        
        // Add click event listener for resign button
        if (resignBtn) {
            resignBtn.addEventListener('click', () => this.handleResignClick());
        }
        
        // Add click event listener for draw button
        if (drawBtn) {
            drawBtn.addEventListener('click', () => this.handleDrawClick());
        }
    }

    /**
     * Handle resign button click
     * Shows confirmation dialog and sends resign request if confirmed
     */
    async handleResignClick() {
        // Check if game is in progress
        if (this.gameStatus !== 'IN_PROGRESS' && this.gameStatus !== 'CHECK') {
            this.showErrorPopup('You can only resign when the game is in progress');
            return;
        }
        
        // Allow resigning regardless of turn (removed the turn check)
        
        // Show confirmation dialog
        const confirmed = await this.showConfirmationDialog(
            'Resign Game',
            'Are you sure you want to resign? This will end the game and your opponent will win.',
            'Resign',
            'Cancel'
        );
        
        if (confirmed) {
            await this.resignGame();
        }
    }

    /**
     * Handle draw button click
     * Sends draw offer request to the opponent
     */
    async handleDrawClick() {
        // Check if game is in progress
        if (this.gameStatus !== 'IN_PROGRESS' && this.gameStatus !== 'CHECK') {
            this.showErrorPopup('You can only offer a draw when the game is in progress');
            return;
        }
        
        // Check if it's the player's turn (they can only offer draw on their turn)
        if (!this.isPlayerTurn) {
            this.showErrorPopup('You can only offer a draw on your turn');
            return;
        }
        
        // Show confirmation dialog
        const confirmed = await this.showConfirmationDialog(
            'Offer Draw',
            'Are you sure you want to offer a draw to your opponent?',
            'Offer Draw',
            'Cancel'
        );
        
        if (confirmed) {
            await this.offerDraw();
        }
    }

    /**
     * Send resign request to the server
     * This method makes an API call to resign the current game
     */
    async resignGame() {
        try {
            // Prepare the request body
            const requestBody = {
                gameId: this.gameId,
                username: this.username
            };
            
            // Send the resign request to the server
            const response = await fetch('/api/games/resign', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(requestBody)
            });
            
            if (!response.ok) {
                const errorData = await response.json();
                this.showErrorPopup(errorData.message || 'Failed to resign game');
                return;
            }
            
            // Game resigned successfully - the WebSocket will handle the game state update
            console.log('Game resigned successfully');
            
        } catch (error) {
            console.error('Error resigning game:', error);
            this.showErrorPopup('Failed to resign game. Please try again.');
        }
    }

    /**
     * Send draw offer request to the server
     * This method makes an API call to offer a draw to the opponent
     */
    async offerDraw() {
        try {
            // Prepare the request body
            const requestBody = {
                gameId: this.gameId,
                username: this.username
            };
            
            // Send the draw offer request to the server
            const response = await fetch('/api/games/offer-draw', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(requestBody)
            });
            
            if (!response.ok) {
                const errorData = await response.json();
                this.showErrorPopup(errorData.message || 'Failed to offer draw');
                return;
            }
            
            // Draw offer sent successfully - the WebSocket will handle the response
            console.log('Draw offer sent successfully');
            
        } catch (error) {
            console.error('Error offering draw:', error);
            this.showErrorPopup('Failed to offer draw. Please try again.');
        }
    }

    /**
     * Handle draw offer from opponent
     * Shows dialog to accept or decline the draw offer
     * @param {string} offeringPlayerUsername The username of the player who offered the draw
     */
    async handleDrawOffer(offeringPlayerUsername) {
        // Show dialog to accept or decline the draw offer
        const action = await this.showDrawOfferDialog(offeringPlayerUsername);
        
        if (action) {
            await this.respondToDrawOffer(action);
        }
    }

    /**
     * Show dialog for responding to draw offer
     * @param {string} offeringPlayerUsername The username of the player who offered the draw
     * @returns {string|null} 'accept', 'decline', or null if cancelled
     */
    showDrawOfferDialog(offeringPlayerUsername) {
        return new Promise((resolve) => {
            // Create the dialog element
            const dialog = document.createElement('div');
            dialog.className = 'draw-offer-dialog';
            dialog.innerHTML = `
                <div class="draw-offer-content">
                    <h3>Draw Offer</h3>
                    <p>${offeringPlayerUsername} has offered a draw.</p>
                    <div class="draw-offer-buttons">
                        <button class="btn-accept-draw">Accept Draw</button>
                        <button class="btn-decline-draw">Decline Draw</button>
                    </div>
                </div>
            `;
            
            // Add event listeners
            const acceptBtn = dialog.querySelector('.btn-accept-draw');
            const declineBtn = dialog.querySelector('.btn-decline-draw');
            
            acceptBtn.addEventListener('click', () => {
                document.body.removeChild(dialog);
                resolve('accept');
            });
            
            declineBtn.addEventListener('click', () => {
                document.body.removeChild(dialog);
                resolve('decline');
            });
            
            // Add to page
            document.body.appendChild(dialog);
            
            // Auto-remove after 15 seconds if no response (shorter time since game continues normally)
            setTimeout(() => {
                if (document.body.contains(dialog)) {
                    document.body.removeChild(dialog);
                    resolve(null);
                }
            }, 15000);
        });
    }

    /**
     * Send response to draw offer
     * @param {string} action 'accept' or 'decline'
     */
    async respondToDrawOffer(action) {
        try {
            // Prepare the request body
            const requestBody = {
                gameId: this.gameId,
                username: this.username,
                action: action
            };
            
            // Send the draw response request to the server
            const response = await fetch('/api/games/respond-draw', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(requestBody)
            });
            
            if (!response.ok) {
                const errorData = await response.json();
                this.showErrorPopup(errorData.message || 'Failed to respond to draw offer');
                return;
            }
            
            // Draw response sent successfully - the WebSocket will handle the game state update
            console.log('Draw response sent successfully:', action);
            
        } catch (error) {
            console.error('Error responding to draw offer:', error);
            this.showErrorPopup('Failed to respond to draw offer. Please try again.');
        }
    }

    /**
     * Handle offer rematch button click
     * Sends rematch offer request to the opponent
     */
    async handleOfferRematch() {
        try {
            // Prepare the request body
            const requestBody = {
                gameId: this.gameId,
                username: this.username
            };
            
            // Send the rematch offer request to the server
            const response = await fetch('/api/games/offer-rematch', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(requestBody)
            });
            
            if (!response.ok) {
                const errorData = await response.json();
                // Close game end popup even on error
                const gameEndPopup = document.getElementById('game-end-popup');
                if (gameEndPopup) {
                    gameEndPopup.remove();
                }
                this.showErrorPopup(errorData.message || 'Failed to offer rematch');
                return;
            }
            
            // Rematch offer sent successfully - close game end popup first
            const gameEndPopup = document.getElementById('game-end-popup');
            if (gameEndPopup) {
                gameEndPopup.remove();
            }
            
            // Rematch offer sent successfully - the WebSocket will handle the response
            console.log('Rematch offer sent successfully');
            
            // Show success feedback to user (not as error popup)
            this.showSuccessMessage('Rematch offer sent to your opponent');
            
        } catch (error) {
            console.error('Error offering rematch:', error);
            this.showErrorPopup('Failed to offer rematch. Please try again.');
        }
    }

    /**
     * Handle new game button click
     * Redirects to the index page to create a new game
     */
    handleNewGame() {
        // Close the game end popup
        const gameEndPopup = document.getElementById('game-end-popup');
        if (gameEndPopup) {
            gameEndPopup.remove();
        }
        
        // Store a flag in sessionStorage to open game creation popup on index page
        sessionStorage.setItem('openGameCreationPopup', 'true');
        
        // Redirect to index page
        window.location.href = '/index';
    }

    /**
     * Handle rematch offer from opponent
     * Shows dialog to accept or decline the rematch offer
     * @param {string} offeringPlayerUsername The username of the player who offered the rematch
     */
    async handleRematchOffer(offeringPlayerUsername) {
        // Show dialog to accept or decline the rematch offer
        const action = await this.showRematchOfferDialog(offeringPlayerUsername);
        
        if (action) {
            await this.respondToRematchOffer(action);
        }
    }

    /**
     * Show dialog for responding to rematch offer
     * @param {string} offeringPlayerUsername The username of the player who offered the rematch
     * @returns {string|null} 'accept', 'decline', or null if cancelled
     */
    showRematchOfferDialog(offeringPlayerUsername) {
        return new Promise((resolve) => {
            // Create the dialog element
            const dialog = document.createElement('div');
            dialog.className = 'rematch-offer-dialog';
            dialog.style.cssText = `
                position: fixed;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                background: rgba(0, 0, 0, 0.9);
                display: flex;
                align-items: center;
                justify-content: center;
                z-index: 100000;
                backdrop-filter: blur(20px);
            `;
            dialog.innerHTML = `
                <div class="rematch-offer-content" style="
                    background: rgba(40, 30, 60, 0.97);
                    color: #fff;
                    padding: 2rem;
                    border-radius: 12px;
                    text-align: center;
                    border: 2px solid var(--color-accent, #ffd700);
                    backdrop-filter: blur(6px);
                ">
                    <h3 style="margin-bottom: 1rem; color: #ffd700;">Rematch Offer</h3>
                    <p style="margin-bottom: 1.5rem;">${offeringPlayerUsername} has offered a rematch.</p>
                    <div class="rematch-offer-buttons" style="display: flex; gap: 1rem; justify-content: center;">
                        <button class="btn-accept-rematch" style="
                            padding: 10px 20px;
                            background: linear-gradient(135deg, #28a745, #20c997);
                            color: white;
                            border: none;
                            border-radius: 6px;
                            cursor: pointer;
                            font-weight: 600;
                        ">Accept Rematch</button>
                        <button class="btn-decline-rematch" style="
                            padding: 10px 20px;
                            background: linear-gradient(135deg, #dc3545, #c82333);
                            color: white;
                            border: none;
                            border-radius: 6px;
                            cursor: pointer;
                            font-weight: 600;
                        ">Decline Rematch</button>
                    </div>
                </div>
            `;
            
            // Add event listeners
            const acceptBtn = dialog.querySelector('.btn-accept-rematch');
            const declineBtn = dialog.querySelector('.btn-decline-rematch');
            
            acceptBtn.addEventListener('click', () => {
                document.body.removeChild(dialog);
                resolve('accept');
            });
            
            declineBtn.addEventListener('click', () => {
                document.body.removeChild(dialog);
                resolve('decline');
            });
            
            // Add to page
            document.body.appendChild(dialog);
            
            // Auto-remove after 30 seconds if no response
            setTimeout(() => {
                if (document.body.contains(dialog)) {
                    document.body.removeChild(dialog);
                    resolve(null);
                }
            }, 30000);
        });
    }

    /**
     * Send response to rematch offer
     * @param {string} action 'accept' or 'decline'
     */
    async respondToRematchOffer(action) {
        try {
            // Prepare the request body
            const requestBody = {
                gameId: this.gameId,
                username: this.username,
                action: action
            };
            
            // Send the rematch response request to the server
            const response = await fetch('/api/games/respond-rematch', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(requestBody)
            });
            
            if (!response.ok) {
                const errorData = await response.json();
                this.showErrorPopup(errorData.message || 'Failed to respond to rematch offer');
                return;
            }
            
            // Rematch response sent successfully - the WebSocket will handle the game state update
            console.log('Rematch response sent successfully:', action);
            
        } catch (error) {
            console.error('Error responding to rematch offer:', error);
            this.showErrorPopup('Failed to respond to rematch offer. Please try again.');
        }
    }

    /**
     * Show confirmation dialog
     * @param {string} title The dialog title
     * @param {string} message The dialog message
     * @param {string} confirmText The confirm button text
     * @param {string} cancelText The cancel button text
     * @returns {Promise<boolean>} True if confirmed, false if cancelled
     */
    showConfirmationDialog(title, message, confirmText, cancelText) {
        return new Promise((resolve) => {
            // Create the dialog element
            const dialog = document.createElement('div');
            dialog.className = 'confirmation-dialog';
            dialog.innerHTML = `
                <div class="confirmation-content">
                    <h3>${title}</h3>
                    <p>${message}</p>
                    <div class="confirmation-buttons">
                        <button class="btn-confirm">${confirmText}</button>
                        <button class="btn-cancel">${cancelText}</button>
                    </div>
                </div>
            `;
            
            // Add event listeners
            const confirmBtn = dialog.querySelector('.btn-confirm');
            const cancelBtn = dialog.querySelector('.btn-cancel');
            
            confirmBtn.addEventListener('click', () => {
                document.body.removeChild(dialog);
                resolve(true);
            });
            
            cancelBtn.addEventListener('click', () => {
                document.body.removeChild(dialog);
                resolve(false);
            });
            
            // Add to page
            document.body.appendChild(dialog);
        });
    }

    /**
     * Show error popup
     * @param {string} message The error message to display
     */
    showErrorPopup(message) {
        // Create the popup element
        const popup = document.createElement('div');
        popup.className = 'error-popup';
        popup.innerHTML = `
            <div class="error-content">
                <h3>Error</h3>
                <p>${message}</p>
                <button class="btn-ok">OK</button>
            </div>
        `;
        
        // Add event listener
        const okBtn = popup.querySelector('.btn-ok');
        okBtn.addEventListener('click', () => {
            document.body.removeChild(popup);
        });
        
        // Add to page
        document.body.appendChild(popup);
    }

    /**
     * Show success message
     * @param {string} message The success message to display
     */
    showSuccessMessage(message) {
        // Create the popup element
        const popup = document.createElement('div');
        popup.className = 'success-popup';
        popup.innerHTML = `
            <div class="success-content">
                <h3>Success</h3>
                <p>${message}</p>
                <button class="btn-ok">OK</button>
            </div>
        `;
        
        // Add event listener
        const okBtn = popup.querySelector('.btn-ok');
        okBtn.addEventListener('click', () => {
            document.body.removeChild(popup);
        });
        
        // Add to page
        document.body.appendChild(popup);
        
        // Auto-remove after 3 seconds
        setTimeout(() => {
            if (document.body.contains(popup)) {
                document.body.removeChild(popup);
            }
        }, 3000);
    }

    // Function to fetch user ID by username
    async fetchUserIdByUsername(username) {
        try {
            const response = await fetch(`/api/users/${username}`);
            if (!response.ok) {
                throw new Error('User not found');
            }
            const user = await response.json();
            if (!user || !user.id) {
                throw new Error('User ID not found in response');
            }
            return user.id;
        } catch (error) {
            return null;
        }
    }

    async fetchGame() {
        // Stop timer during server sync to prevent conflicts
        this.stopTimer();
        this.isUpdatingFromServer = true; // Prevent timer updates during server sync
        
        const response = await fetch(`/api/games/${this.gameId}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        if (!response.ok) {
            const errorText = await response.text();
            this.isUpdatingFromServer = false; // Clear flag on error
            throw new Error('Fetching game failed');
        }
        
        // Get the response data
        const gameData = await response.json();
        
        // Sync client time with server time for accurate timer calculations
        if (gameData.serverTime) {
            // Calculate time offset between client and server (in milliseconds)
            const serverTime = new Date(gameData.serverTime);
            const clientTime = new Date();
            this.serverTimeOffset = serverTime.getTime() - clientTime.getTime();
        }
        
        // Check if the board data is directly in the response or nested
        if (gameData.board) {
            // Check if board is a string (JSON) that needs to be parsed
            if (typeof gameData.board === 'string') {
                try {
                    this.boardDTO = JSON.parse(gameData.board);
                } catch (e) {
                    this.boardDTO = gameData.board;
                }
            } else {
                this.boardDTO = gameData.board;
            }
        } else {
            // If the board data is directly in the response
            this.boardDTO = gameData;
        }
        
        // Copy player usernames to boardDTO
        this.boardDTO.whitePlayerUsername = gameData.whitePlayerUsername;
        this.boardDTO.blackPlayerUsername = gameData.blackPlayerUsername;
        this.boardDTO.whitePlayerAvatar = gameData.whitePlayerAvatar;
        this.boardDTO.blackPlayerAvatar = gameData.blackPlayerAvatar;
        
        // Add player IDs to boardDTO for winner determination
        this.boardDTO.whitePlayerId = gameData.whitePlayerId;
        this.boardDTO.blackPlayerId = gameData.blackPlayerId;
        
        // Store winner information from database
        if (gameData.winnerId) {
            this.winnerId = gameData.winnerId;
            console.log('Received winnerId from database:', gameData.winnerId);
        }
        
        this.gameStatus = gameData.status;
        this.gameId = gameData.id;
        this.lastMoveData = gameData.lastMoveData;

        // Store the latest game state for timer calculations (when in viewing mode)
        this.latestGameState = {
            currentPlayer: this.boardDTO.currentPlayer,
            gameStatus: this.gameStatus,
            lastMoveAt: this.lastMoveAt,
            whiteTimeLeftSeconds: this.whiteTimeLeftSeconds,
            blackTimeLeftSeconds: this.blackTimeLeftSeconds
        };

        // Reset game end popup flag if game status changes from end state to IN_PROGRESS (new game)
        if (this.gameStatus === 'IN_PROGRESS' && this.lastGameStatus && 
            (this.lastGameStatus === 'CHECKMATE' || this.lastGameStatus === 'DRAW' || this.lastGameStatus === 'RESIGNED'
            || this.lastGameStatus === 'STALEMATE' || this.lastGameStatus === 'THREEFOLD_REPETITION'
            || this.lastGameStatus === 'FIFTY_MOVE_RULE' || this.lastGameStatus === 'INSUFFICIENT_MATERIAL'
            || this.lastGameStatus === 'MUTUAL_AGREEMENT' || this.lastGameStatus === 'TIME_OUT')) {
            this.gameEndPopupShown = false;
            this.timeoutDetected = false; // Reset timeout flag for new game
        }

        // Set player color and board orientation
        if (gameData.whitePlayerId === this.userId) {
            this.playerColor = 'WHITE';
            this.board.classList.remove('black-perspective');
        } else if (gameData.blackPlayerId === this.userId) {
            this.playerColor = 'BLACK';
            this.board.classList.add('black-perspective');
        }

        // Initialize timer data from server
        this.whiteTimeLeftSeconds = gameData.whiteTimeLeftSeconds;
        this.blackTimeLeftSeconds = gameData.blackTimeLeftSeconds;
        this.lastMoveAt = gameData.lastMoveAt;
        this.timeControlMinutes = gameData.timeControlMinutes;

        // Check if timer needs to be started (when inviter is redirected to game page)
        if ((this.gameStatus === 'IN_PROGRESS' || this.gameStatus === 'CHECK') && 
            this.timeControlMinutes && 
            !this.lastMoveAt) {
            await this.startGameTimer();
        }

        // Start timer if game has started and timer is enabled
        if ((this.gameStatus === 'IN_PROGRESS' || this.gameStatus === 'CHECK') && this.timeControlMinutes) {
            this.startTimer();
        } else if (this.gameStatus !== 'IN_PROGRESS' && this.gameStatus !== 'CHECK') {
            // Stop timer only if game is not in progress and not in check
            this.stopTimer();
        }

        // Reinitialize the board with correct coordinate labels
        this.initializeBoard();

        if(this.boardDTO.currentPlayer.alliance === this.playerColor) {
            this.isPlayerTurn = true;
        } else {
            this.isPlayerTurn = false;
        }
        this.updateBoard();
        
        // Reload game positions to update navigation buttons
        if (!this.isViewingMode) {
            await this.loadGamePositions();
        }
        
        // Clear server sync flag and restart timer if game is active
        this.isUpdatingFromServer = false;
        
        // Restart timer if game is in progress and timer is enabled
        if ((this.gameStatus === 'IN_PROGRESS' || this.gameStatus === 'CHECK') && this.timeControlMinutes) {
            this.startTimer();
        }
    }

    connectWebSocket() {
        // Make sure we have a valid gameId before attempting to connect
        if (!this.gameId) {
            return;
        }
        
        // Make sure we have a valid userId before attempting to connect
        if (!this.userId) {
            return;
        }
        
        const socket = new SockJS('/chess-websocket');
        this.stompClient = Stomp.over(socket);
        
        this.stompClient.debug = function(str) {
            // Silent debug for speed
        };
    
        this.stompClient.connect({}, 
            (frame) => {
                this.reconnectAttempts = 0;
                
                // Subscribe to game moves
                if (this.stompClient && this.stompClient.connected) {
                    this.stompClient.subscribe('/topic/game/' + this.gameId, async (message) => {
                        try {
                            const moveData = JSON.parse(message.body);
                            
                            // Handle different message types
                            if (moveData.boardDTO === 'GAME_STARTED') {
                                // Update opponent's username in the UI
                                const opponentPlayerName = document.getElementById('opponent-player-name');
                                if (opponentPlayerName) {
                                    if (this.userId === moveData.whitePlayerId) {
                                        // If I'm white, opponent is black
                                        opponentPlayerName.textContent = moveData.blackPlayerUsername;
                                    } else if (this.userId === moveData.blackPlayerId) {
                                        // If I'm black, opponent is white
                                        opponentPlayerName.textContent = moveData.whitePlayerUsername;
                                    }
                                }

                                this.showGameStartAnimation();
                                
                                // Fetch and update the game state
                                await this.fetchGame();
                                
                                // Timer is automatically started by fetchGame() if game is active
                                
                                // Update the status based on player color
                                if (this.playerColor === 'WHITE') {
                                    this.statusElement.textContent = 'Your turn';
                                    this.isPlayerTurn = true;
                                } else {
                                    this.statusElement.textContent = 'Opponent\'s turn';
                                    this.isPlayerTurn = false;
                                }
                            } else if (moveData.type === 'MOVE_MADE') {
                                // Debug: Log the full message
                                console.log('Full MOVE_MADE WebSocket message:', moveData);
                                
                                // Store winner ID if provided in the message
                                if (moveData.winnerId) {
                                    this.winnerId = moveData.winnerId;
                                    console.log('Received winnerId from MOVE_MADE:', moveData.winnerId);
                                } else {
                                    console.log('No winnerId in MOVE_MADE message');
                                }
                                
                                await this.fetchGame();
                                
                                // Timer is automatically restarted by fetchGame() after server sync
                                
                                // Map move types to sound types
                                const moveTypeToSound = {
                                    'NORMAL': 'move',
                                    'CAPTURE': 'capture',
                                    'CHECK': 'check',
                                    'CHECKMATE': 'check',
                                    'CASTLE': 'move',
                                    'EN_PASSANT': 'capture',
                                    'PAWN_PROMOTION': 'move',
                                    'PAWN_JUMP': 'move'
                                };
                                
                                // Play sound based on move type
                                if (moveData.moveType) {
                                    const soundType = moveTypeToSound[moveData.moveType] || 'move';
                                    this.playSound(soundType);
                                } else {
                                    this.playSound('move');
                                }
                                
                                if (this.gameStatus === 'IN_PROGRESS') {
                                    // Check if it's the current player's turn
                                    const isCurrentPlayerTurn = 
                                        (this.playerColor === 'WHITE' && this.boardDTO.currentPlayer.alliance === 'WHITE') ||
                                        (this.playerColor === 'BLACK' && this.boardDTO.currentPlayer.alliance === 'BLACK');
                                    
                                    this.statusElement.textContent = isCurrentPlayerTurn ? 'Your turn' : 'Opponent\'s turn';
                                }
                                else {
                                    this.statusElement.textContent = this.gameStatus;
                                }
                            } else if (moveData.type === 'TIME_OUT') {
                                // Debug: Log the full message
                                console.log('Full TIME_OUT WebSocket message:', moveData);
                                
                                // Store winner ID if provided in the message
                                if (moveData.winnerId) {
                                    this.winnerId = moveData.winnerId;
                                    console.log('Received winnerId from TIME_OUT:', moveData.winnerId);
                                } else {
                                    console.log('No winnerId in TIME_OUT message');
                                }
                                
                                this.stopTimer();
                                this.timeoutDetected = true; // Set flag to disable interactions
                                await this.fetchGame();
                                
                                // Update game status and show end game popup if it's timeout
                                if (this.gameStatus === 'TIME_OUT') {
                                    this.statusElement.textContent = 'Game over by timeout!';
                                    // The updateBoard method will handle showing the end game popup
                                    this.updateBoard();
                                }
                            } else if (moveData.type === 'GAME_RESIGNED') {
                                // Debug: Log the full message
                                console.log('Full GAME_RESIGNED WebSocket message:', moveData);
                                
                                // Store winner ID if provided in the message
                                if (moveData.winnerId) {
                                    this.winnerId = moveData.winnerId;
                                    console.log('Received winnerId from GAME_RESIGNED:', moveData.winnerId);
                                } else {
                                    console.log('No winnerId in GAME_RESIGNED message');
                                }
                                
                                this.stopTimer();
                                await this.fetchGame();
                                
                                // Update game status and show end game popup
                                if (this.gameStatus === 'RESIGNED') {
                                    this.statusElement.textContent = 'Game over by resignation!';
                                    // The updateBoard method will handle showing the end game popup
                                    this.updateBoard();
                                }
                            } else if (moveData.type === 'DRAW_OFFERED') {
                                // Debug: Log the full message
                                console.log('Full DRAW_OFFERED WebSocket message:', moveData);
                                
                                // Check if this player is the one who should respond to the draw offer
                                // (i.e., not the one who offered it)
                                if (moveData.offeringPlayerUsername !== this.username) {
                                    // Show draw offer dialog to the opponent
                                    this.handleDrawOffer(moveData.offeringPlayerUsername);
                                }
                                
                                // Don't change the status - keep the current game status
                                // The game continues normally while draw offer is pending
                            } else if (moveData.type === 'DRAW_RESPONSE') {
                                // Debug: Log the full message
                                console.log('Full DRAW_RESPONSE WebSocket message:', moveData);
                                
                                // Store winner ID if provided in the message (for accepted draws)
                                if (moveData.winnerId) {
                                    this.winnerId = moveData.winnerId;
                                    console.log('Received winnerId from DRAW_RESPONSE:', moveData.winnerId);
                                }
                                
                                // Handle the draw response
                                if (moveData.action === 'accept') {
                                    // Draw was accepted
                                    this.stopTimer();
                                    await this.fetchGame();
                                    
                                    if (this.gameStatus === 'MUTUAL_AGREEMENT') {
                                        this.statusElement.textContent = 'Game ended in draw by mutual agreement!';
                                        // The updateBoard method will handle showing the end game popup
                                        this.updateBoard();
                                    }
                                } else if (moveData.action === 'decline') {
                                    // Draw was declined - just fetch the current game state
                                    await this.fetchGame();
                                    
                                    // Update status based on current game state
                                    if (this.gameStatus === 'IN_PROGRESS' || this.gameStatus === 'CHECK') {
                                        // Check if it's the current player's turn
                                        const isCurrentPlayerTurn = 
                                            (this.playerColor === 'WHITE' && this.boardDTO.currentPlayer.alliance === 'WHITE') ||
                                            (this.playerColor === 'BLACK' && this.boardDTO.currentPlayer.alliance === 'BLACK');
                                        
                                        this.statusElement.textContent = isCurrentPlayerTurn ? 'Your turn' : 'Opponent\'s turn';
                                    }
                                }
                            } else if (moveData.type === 'REMATCH_OFFERED') {  // ← Now at correct level
                                // Debug: Log the full message
                                console.log('Full REMATCH_OFFERED WebSocket message:', moveData);
                                
                                // Check if this player is the one who should respond to the rematch offer
                                // (i.e., not the one who offered it)
                                if (moveData.offeringPlayerUsername !== this.username) {
                                    // Show rematch offer dialog to the opponent
                                    this.handleRematchOffer(moveData.offeringPlayerUsername);
                                }
         
                            } else if (moveData.type === 'REMATCH_RESPONSE') {  // ← Now at correct level
                                // Debug: Log the full message
                                console.log('Full REMATCH_RESPONSE WebSocket message:', moveData);
                                
                                // Handle the rematch response
                                if (moveData.action === 'accept') {
                                    // Rematch was accepted - close game end popup first
                                    const gameEndPopup = document.getElementById('game-end-popup');
                                    if (gameEndPopup) {
                                        gameEndPopup.remove();
                                    }
                                    
                                    // Redirect to new game
                                    if (moveData.newGameId) {
                                        this.showSuccessMessage('Rematch accepted! Redirecting to new game...');
                                        setTimeout(() => {
                                            window.location.href = `/games/${moveData.newGameId}`;
                                        }, 2000);
                                    }
                                } else if (moveData.action === 'decline') {
                                    // Rematch was declined - no message shown
                                }
                            }
                        } catch (error) {
                            // Silent error handling for speed
                        }
                    });
                    
                    // Subscribe to chat messages
                    this.stompClient.subscribe('/topic/chat/' + this.gameId, (message) => {
                        try {
                            const chatMessage = JSON.parse(message.body);
                            this.displayChatMessage(chatMessage);
                        } catch (error) {
                            // Silent error handling for speed
                        }
                    });
                }
            },
            (error) => {
                // Silent error handling for speed
            }
        );
    }

    sendChatMessage() {
        const message = this.chatInput.value.trim();
        if (!message || !this.stompClient || !this.stompClient.connected) return;
        
        const chatMessage = {
            gameId: this.gameId,
            message: message,
            sender: this.username,
            timestamp: Date.now()
        };
        
        this.stompClient.send('/app/chat/' + this.gameId, {}, JSON.stringify(chatMessage));
        this.chatInput.value = '';
    }
    
    displayChatMessage(chatMessage) {
        // Create message element for small chat
        const messageDiv = document.createElement('div');
        messageDiv.className = 'chat-message';
        
        const senderDiv = document.createElement('div');
        senderDiv.className = 'sender';
        senderDiv.textContent = chatMessage.sender;
        
        const contentDiv = document.createElement('div');
        contentDiv.className = 'content';
        contentDiv.textContent = chatMessage.message;
        
        const timestampDiv = document.createElement('div');
        timestampDiv.className = 'timestamp';
        const date = new Date(chatMessage.timestamp);
        timestampDiv.textContent = date.toLocaleTimeString();
        
        messageDiv.appendChild(senderDiv);
        messageDiv.appendChild(contentDiv);
        messageDiv.appendChild(timestampDiv);
        
        // Add to small chat
        this.chatMessages.appendChild(messageDiv);
        this.chatMessages.scrollTop = this.chatMessages.scrollHeight;
        
        // Create message element for enlarged chat modal
        const modalMessageDiv = document.createElement('div');
        modalMessageDiv.className = 'chat-modal-message';
        
        const modalSenderDiv = document.createElement('div');
        modalSenderDiv.className = 'sender';
        modalSenderDiv.textContent = chatMessage.sender;
        
        const modalContentDiv = document.createElement('div');
        modalContentDiv.className = 'content';
        modalContentDiv.textContent = chatMessage.message;
        
        const modalTimestampDiv = document.createElement('div');
        modalTimestampDiv.className = 'timestamp';
        modalTimestampDiv.textContent = date.toLocaleTimeString();
        
        modalMessageDiv.appendChild(modalSenderDiv);
        modalMessageDiv.appendChild(modalContentDiv);
        modalMessageDiv.appendChild(modalTimestampDiv);
        
        // Add to enlarged chat modal
        this.chatModalMessages.appendChild(modalMessageDiv);
        this.chatModalMessages.scrollTop = this.chatModalMessages.scrollHeight;
    }

    async handleTileClick(event) {
        // Prevent moves if timeout has been detected
        if (this.timeoutDetected) {
            return;
        }
        
        // Prevent moves if game hasn't started
        if (this.gameStatus === 'WAITING_FOR_OPPONENT') {
            return;
        }
        
        // Prevent moves if game is not in progress (ended)
        if (this.gameStatus !== 'IN_PROGRESS' && this.gameStatus !== 'CHECK') {
            return;
        }
        
        // Prevent moves if in viewing mode (not at latest board)
        if (this.isViewingMode) {
            return;
        }
        
        if (!this.isPlayerTurn) {
            return;
        }
        const tile = event.target.closest('.tile');
        if (!tile) return;

        let position = parseInt(tile.dataset.position);

        // Clear previous selection first
        document.querySelector('.selected')?.classList.remove('selected');

        if (this.selectedSourceTile === null) {
            // First click - select piece
            if (this.hasPiece(tile)) {
                const tileData = this.boardDTO.tiles.find(t => t.tileCoordinate === position);
                
                if (!tileData || !tileData.piece) {
                    return;
                }
                
                const piece = tileData.piece;
                if ((this.playerColor === 'WHITE' && piece.pieceAlliance === 'WHITE') || 
                    (this.playerColor === 'BLACK' && piece.pieceAlliance === 'BLACK')) {
                    this.selectedSourceTile = position;
                    tile.classList.add('selected');
                    this.showLegalMoves(position);
                } else {
                    document.querySelector('.selected')?.classList.remove('selected');
                    this.selectedSourceTile = null;
                    this.clearLegalMoves();
                }
            }
        } else {
            // Second click - make move
            if (this.hasPiece(tile)) {
                const tileData = this.boardDTO.tiles.find(t => t.tileCoordinate === position);
                const piece = tileData?.piece;
                
                if (piece && piece.pieceAlliance === this.boardDTO.currentPlayer.alliance) {
                    // If clicking on another piece of the same color, select that piece instead
                    document.querySelector('.selected')?.classList.remove('selected');
                    this.selectedSourceTile = position;
                    tile.classList.add('selected');
                    this.clearLegalMoves();
                    this.showLegalMoves(position);
                    return;
                }

                // Check if the target position is a legal move
                const moves = this.boardDTO.currentPlayer.moves;
                const isLegalMove = moves.some(move => 
                    move.sourceCoordinate === this.selectedSourceTile && 
                    move.targetCoordinate === position
                );

                if (!isLegalMove) {
                    // If not a legal move, clear selection and highlights
                    document.querySelector('.selected')?.classList.remove('selected');
                    this.selectedSourceTile = null;
                    this.clearLegalMoves();
                    return;
                }
            }
            
            // Client-side move execution for immediate visual feedback
            await this.executeClientSideMove(this.selectedSourceTile, position);
        }
    }

    handleMouseDown(event) {
        // Prevent dragging if timeout has been detected
        if (this.timeoutDetected) {
            return;
        }
        
        if(!this.isPlayerTurn) {
            return; // Early return for speed
        }
        
        // Prevent dragging if game is not in progress (ended)
        if (this.gameStatus !== 'IN_PROGRESS' && this.gameStatus !== 'CHECK') {
            return; // Early return for speed
        }
        
        // Prevent dragging if in viewing mode (browsing positions)
        if (this.isViewingMode) {
            return; // Early return for speed
        }
        
        const piece = event.target.closest('.piece');
        if (!piece) return;

        const tile = piece.parentElement;
        const position = parseInt(tile.dataset.position);
        const tileData = this.boardDTO.tiles.find(t => t.tileCoordinate === position);
        
        if (!tileData || !tileData.piece) return;

        const pieceData = tileData.piece;
        if (pieceData.pieceAlliance !== this.boardDTO.currentPlayer.alliance) return;

        // Enhanced visual feedback for drag start
        this.isDragging = true;
        this.draggedPiece = piece;
        this.selectedSourceTile = position;

        // Create drag image with enhanced visual feedback
        this.dragImage = document.createElement('div');
        this.dragImage.className = 'piece dragging-piece';
        this.dragImage.style.backgroundImage = piece.style.backgroundImage;
        this.dragImage.style.transform = 'translate(-50%, -50%)'; // Keep original size
        this.dragImage.style.filter = 'drop-shadow(0 4px 8px rgba(0, 0, 0, 0.3))'; // Add shadow for depth
        document.body.appendChild(this.dragImage);

        // Set initial position directly for maximum speed
        const offsetX = 10; // Small offset for better visual feedback
        const offsetY = 10;
        this.dragImage.style.left = (event.clientX + offsetX) + 'px';
        this.dragImage.style.top = (event.clientY + offsetY) + 'px';

        // Enhanced original piece feedback - more visible but clearly indicates it's being dragged
        this.draggedPiece.style.opacity = '0.4';
        this.draggedPiece.style.transform = 'scale(0.9)'; // Slightly smaller to show it's being moved

        // Show legal moves with optimized performance and enhanced visual feedback
        this.clearLegalMoves();
        tile.classList.add('selected');
        this.showLegalMoves(position);

        // Add immediate hover feedback for the source tile
        tile.classList.add('dragover', 'dragover-valid');

        event.preventDefault();
    }

    handleMouseMove(event) {
        // Prevent mouse movement if timeout has been detected
        if (this.timeoutDetected) {
            return;
        }
        
        if(!this.isPlayerTurn) {
            return; // Early return for speed
        }
        if (!this.isDragging || !this.dragImage) return;
        
        // Use requestAnimationFrame for ultra-smooth updates
        requestAnimationFrame(() => {
            // Ultra-fast position update with slight offset for better feel
            const offsetX = 10; // Small offset for better visual feedback
            const offsetY = 10;
            this.dragImage.style.left = (event.clientX + offsetX) + 'px';
            this.dragImage.style.top = (event.clientY + offsetY) + 'px';
        });
        
        // Enhanced hover effect with better tile detection
        const hoveredElement = document.elementFromPoint(event.clientX, event.clientY);
        const hoveredTile = hoveredElement?.closest('.tile');
        
        // Only update if the hovered tile has changed for better performance
        if (hoveredTile !== this.lastHoveredTile) {
            // Remove previous hover effects
            if (this.lastHoveredTile) {
                this.lastHoveredTile.classList.remove('dragover', 'dragover-valid', 'dragover-invalid');
            }
            
            // Add new hover effects with enhanced visual feedback
            if (hoveredTile) {
                const targetPosition = parseInt(hoveredTile.dataset.position);
                const sourcePosition = this.selectedSourceTile;
                
                // Check if this is a valid move by looking at legal moves
                const isValidMove = this.isValidMove(sourcePosition, targetPosition);
                
                if (isValidMove) {
                    // Add valid move hover effect - piece will land here
                    hoveredTile.classList.add('dragover', 'dragover-valid');
                } else if (sourcePosition !== targetPosition) {
                    // Add invalid move hover effect - piece cannot land here
                    hoveredTile.classList.add('dragover', 'dragover-invalid');
                }
            }
            
            this.lastHoveredTile = hoveredTile;
        }
        
        event.preventDefault();
    }

    // Helper method to check if a move is valid
    isValidMove(sourcePosition, targetPosition) {
        // Early return if same position
        if (sourcePosition === targetPosition) {
            return false;
        }
        
        // Check if boardDTO and currentPlayer exist
        if (!this.boardDTO || !this.boardDTO.currentPlayer || !this.boardDTO.currentPlayer.moves) {
            return false;
        }
        
        // Check if this move exists in the legal moves array
        const moves = this.boardDTO.currentPlayer.moves;
        return moves.some(move => 
            move.sourceCoordinate === sourcePosition && 
            move.targetCoordinate === targetPosition
        );
    }

    async handleMouseUp(event) {
        // Prevent moves if game hasn't started
        if (this.gameStatus === 'WAITING_FOR_OPPONENT') {
            return;
        }
        
        // Prevent moves if game is not in progress (ended)
        if (this.gameStatus !== 'IN_PROGRESS' && this.gameStatus !== 'CHECK') {
            return;
        }
        
        if (!this.isPlayerTurn) {
            return;
        }
        if (!this.isDragging) return;

        // Remove any remaining dragover effects with enhanced cleanup
        document.querySelectorAll('.tile.dragover, .tile.dragover-valid, .tile.dragover-invalid').forEach(tile => {
            tile.classList.remove('dragover', 'dragover-valid', 'dragover-invalid');
        });

        // If right click, just cancel the drag
        if (event.button === 2) {
            // Cleanup
            if (this.draggedPiece) {
                this.draggedPiece.style.opacity = '1';
                this.draggedPiece.style.transform = 'scale(1)'; // Reset scale
            }
            if (this.dragImage) {
                this.dragImage.remove();
            }
            this.isDragging = false;
            this.draggedPiece = null;
            this.dragImage = null;
            this.selectedSourceTile = null;
            this.lastHoveredTile = null; // Reset hover tracking
            this.clearLegalMoves();
            document.querySelector('.selected')?.classList.remove('selected');
            return;
        }

        const targetTile = event.target.closest('.tile');
        if (targetTile) {
            const targetPosition = parseInt(targetTile.dataset.position);
            if (this.selectedSourceTile !== targetPosition) {
                // Use client-side move execution for immediate visual feedback
                await this.executeClientSideMove(this.selectedSourceTile, targetPosition);
            }
        }
        
        // Cleanup
        if (this.draggedPiece) {
            this.draggedPiece.style.opacity = '1';
            this.draggedPiece.style.transform = 'scale(1)'; // Reset scale
        }
        if (this.dragImage) {
            this.dragImage.remove();
        }
        this.isDragging = false;
        this.draggedPiece = null;
        this.dragImage = null;
        this.lastHoveredTile = null; // Reset hover tracking
    }

    showLegalMoves(position) {
        // Check if boardDTO and currentPlayer exist
        if (!this.boardDTO || !this.boardDTO.currentPlayer) {
            return;
        }
        
        // Get the moves array from currentPlayer
        const moves = this.boardDTO.currentPlayer.moves;
        
        // Validate moves array
        if (!moves || !Array.isArray(moves)) {
            return;
        }
        
        // Filter moves for the selected position
        const legalMoves = moves.filter(move => move.sourceCoordinate === position);
        // Highlight each legal move
        legalMoves.forEach(move => {
            const targetTile = this.board.querySelector(`.tile[data-position='${move.targetCoordinate}']`);
            if (targetTile) {
                // Check if the move is a capture
                const targetTileData = this.boardDTO.tiles.find(t => t.tileCoordinate === move.targetCoordinate);
                const isCapture = targetTileData && targetTileData.tileOccupied;
                // Add appropriate highlight class - using the correct CSS class names
                targetTile.classList.add(isCapture ? 'legal-move-capture' : 'legal-move-non-capture');
            }
        });

        // --- En passant highlight for the clicked pawn only ---
        if (this.lastMoveData) {
            try {
                const lastMoveDataObj = JSON.parse(this.lastMoveData);
                const lastMoveTarget = lastMoveDataObj.targetCoordinate;
                const lastMoveType = lastMoveDataObj.moveType;
                if (lastMoveType === 'PAWN_JUMP') {
                    const tileData = this.boardDTO.tiles.find(t => t.tileCoordinate === position);
                    if (tileData && tileData.piece && tileData.piece.pieceSymbol === 'PAWN') {
                        const pawn = tileData.piece;
                        const pawnCoord = tileData.tileCoordinate;
                        const direction = pawn.pieceAlliance === 'WHITE' ? -8 : 8;
                        const pawnRank = Math.floor(pawnCoord / 8);
                        const jumpedPawnRank = Math.floor(lastMoveTarget / 8);
                        // Check adjacency, same rank, and diagonal
                        if (
                            pawnRank === jumpedPawnRank &&
                            (
                                (pawnCoord === lastMoveTarget + 1 && pawnCoord % 8 !== 0) ||
                                (pawnCoord === lastMoveTarget - 1 && pawnCoord % 8 !== 7)
                            )
                        ) {
                            const enPassantTarget = lastMoveTarget + direction;
                            if (
                                Math.abs(enPassantTarget - pawnCoord) === 7 ||
                                Math.abs(enPassantTarget - pawnCoord) === 9
                            ) {
                                const enPassantTile = this.boardDTO.tiles.find(t => t.tileCoordinate === enPassantTarget && !t.tileOccupied);
                                if (enPassantTile) {
                                    const targetTile = this.board.querySelector(`.tile[data-position='${enPassantTarget}']`);
                                    if (targetTile) {
                                        targetTile.classList.add('legal-move-en-passant');
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e) {
                // Silent error handling for speed
            }
        }
    }

    clearLegalMoves() {
        // Remove all legal move highlights - using the correct CSS class names
        document.querySelectorAll('.legal-move-non-capture, .legal-move-capture, .legal-move-en-passant').forEach(tile => {
            tile.classList.remove('legal-move-non-capture', 'legal-move-capture', 'legal-move-en-passant');
        });
        
        // Removed king check highlighting when clearing legal moves
    }

    hasPiece(tile) {
        return tile.querySelector('.piece') !== null;
    }

    updateBoard() {        
        // Track game status changes for popup display
        const gameStatusChanged = this.lastGameStatus !== this.gameStatus;
        this.lastGameStatus = this.gameStatus;
        
        // If game hasn't started, show waiting message
        if (this.gameStatus === 'WAITING_FOR_OPPONENT') {
            this.statusElement.textContent = 'Waiting for opponent to join...';
            this.statusElement.classList.remove('your-turn');
            this.isPlayerTurn = false;
            return;
        }
        
        // Only update game status and turn indicator if not in viewing mode
        if (!this.isViewingMode) {
        // Update game status and turn indicator
        if (this.boardDTO.currentPlayer.alliance === this.playerColor) {
            this.statusElement.textContent = 'Your turn to move';
            this.statusElement.classList.add('your-turn');
            this.isPlayerTurn = true;
        } else {
            this.statusElement.textContent = `Waiting for ${this.boardDTO.currentPlayer.alliance.toLowerCase()} to move`;
            this.statusElement.classList.remove('your-turn');
            this.isPlayerTurn = false;
            }
        }

        // Check for game end conditions - show popup only once when status changes to end state
        if ((this.gameStatus === 'CHECKMATE' || this.gameStatus === 'DRAW' || this.gameStatus === 'RESIGNED'
            || this.gameStatus === 'STALEMATE' || this.gameStatus === 'THREEFOLD_REPETITION'
            || this.gameStatus === 'FIFTY_MOVE_RULE' || this.gameStatus === 'INSUFFICIENT_MATERIAL'
            || this.gameStatus === 'MUTUAL_AGREEMENT' || this.gameStatus === 'TIME_OUT')
            && gameStatusChanged && !this.gameEndPopupShown
        ) {
            // Mark that we've shown the popup for this game end
            this.gameEndPopupShown = true;
            
            // Stop the timer when game ends
            this.stopTimer();
            
            // Update timer display to show final times (only if not updating from server)
            if (!this.isUpdatingFromServer) {
                this.updateTimerDisplay();
            }
            
            // Add visual indicator that timer has stopped
            const timerElements = document.querySelectorAll('.player-timer');
            timerElements.forEach(timer => {
                timer.classList.add('timer-stopped');
            });
            
            this.statusElement.classList.remove('your-turn');
            // Always display white on left, black on right
            const whitePlayerAvatar = this.boardDTO.whitePlayerAvatar || '/images/default-avatar.png';
            const blackPlayerAvatar = this.boardDTO.blackPlayerAvatar || '/images/default-avatar.png';
            const whitePlayerUsername = this.boardDTO.whitePlayerUsername;
            const blackPlayerUsername = this.boardDTO.blackPlayerUsername;
            let winner, result;

            if (this.gameStatus === 'TIME_OUT') {
                // Use the winner information from the WebSocket message or game data
                if (this.winnerId) {
                    // Use winnerId to determine winner username
                    if (this.winnerId === this.boardDTO.whitePlayerId) {
                        winner = this.boardDTO.whitePlayerUsername;
                    } else if (this.winnerId === this.boardDTO.blackPlayerId) {
                        winner = this.boardDTO.blackPlayerUsername;
                    } else {
                        // Fallback logic if winnerId doesn't match known players
                        if (this.boardDTO.currentPlayer.alliance === 'WHITE') {
                            winner = this.boardDTO.blackPlayerUsername;
                        } else {
                            winner = this.boardDTO.whitePlayerUsername;
                        }
                    }
                } else if (this.winnerUsername) {
                    winner = this.winnerUsername;
                } else {
                    // Fallback logic if winner is not set
                    if (this.boardDTO.currentPlayer.alliance === 'WHITE') {
                        // White ran out of time, so black wins
                        winner = this.boardDTO.blackPlayerUsername;
                    } else {
                        // Black ran out of time, so white wins
                        winner = this.boardDTO.whitePlayerUsername;
                    }
                }
            } else if (this.gameStatus === 'CHECKMATE') {
                // Use the winner information from the WebSocket message or determine based on current player
                if (this.winnerId) {
                    // Use winnerId to determine winner username
                    if (this.winnerId === this.boardDTO.whitePlayerId) {
                        winner = this.boardDTO.whitePlayerUsername;
                    } else if (this.winnerId === this.boardDTO.blackPlayerId) {
                        winner = this.boardDTO.blackPlayerUsername;
                    } else {
                        // Fallback logic if winnerId doesn't match known players
                        if (this.boardDTO.currentPlayer.alliance === 'WHITE') {
                            // White's turn but checkmate, so black won
                            winner = this.boardDTO.blackPlayerUsername;
                        } else {
                            // Black's turn but checkmate, so white won
                            winner = this.boardDTO.whitePlayerUsername;
                        }
                    }
                } else {
                    // Fallback logic if winnerId is not available
                    if (this.boardDTO.currentPlayer.alliance === 'WHITE') {
                        // White's turn but checkmate, so black won
                        winner = this.boardDTO.blackPlayerUsername;
                    } else {
                        // Black's turn but checkmate, so white won
                        winner = this.boardDTO.whitePlayerUsername;
                    }
                }
            } else if (this.gameStatus === 'DRAW') {
                winner = 'Draw';
            } else if (this.gameStatus === 'RESIGNED') {
                // Use the winner information from the WebSocket message or determine based on current player
                if (this.winnerId) {
                    // Use winnerId to determine winner username
                    if (this.winnerId === this.boardDTO.whitePlayerId) {
                        winner = this.boardDTO.whitePlayerUsername;
                    } else if (this.winnerId === this.boardDTO.blackPlayerId) {
                        winner = this.boardDTO.blackPlayerUsername;
                    } else {
                        // Fallback logic if winnerId doesn't match known players
                        if (this.boardDTO.currentPlayer.alliance === 'WHITE') {
                            // White resigned, so black won
                            winner = this.boardDTO.blackPlayerUsername;
                        } else {
                            // Black resigned, so white won
                            winner = this.boardDTO.whitePlayerUsername;
                        }
                    }
                } else {
                    // Fallback logic if winnerId is not available
                    if (this.boardDTO.currentPlayer.alliance === 'WHITE') {
                        // White resigned, so black won
                        winner = this.boardDTO.blackPlayerUsername;
                    } else {
                        // Black resigned, so white won
                        winner = this.boardDTO.whitePlayerUsername;
                    }
                }
            } else if(this.gameStatus === 'STALEMATE') {
                this.statusElement.textContent = 'Draw by stalemate!';
                winner = 'Draw';
            } else if(this.gameStatus === 'THREEFOLD_REPETITION') {
                this.statusElement.textContent = 'Draw by threefold repetition!';
                winner = 'Draw';
            } else if(this.gameStatus === 'FIFTY_MOVE_RULE') {
                this.statusElement.textContent = 'Draw by fifty move rule!';
                winner = 'Draw';
            } else if(this.gameStatus === 'INSUFFICIENT_MATERIAL') {
                this.statusElement.textContent = 'Draw by insufficient material!';
                winner = 'Draw';
            } else if(this.gameStatus === 'MUTUAL_AGREEMENT') {
                this.statusElement.textContent = 'Draw by mutual agreement!';
                winner = 'Draw';
            }

            // Determine result based on winner vs current player
            if (winner === 'Draw') {
                result = 'Draw';
            } else {
                // Check if winner's alliance matches current player's alliance
                const winnerAlliance = (winner === this.boardDTO.whitePlayerUsername) ? 'WHITE' : 'BLACK';
                const currentPlayerAlliance = this.playerColor;
                
                if (winnerAlliance === currentPlayerAlliance) {
                    result = '1-0'; // Current player won
                } else {
                    result = '0-1'; // Opponent won
                }
            }

            // Always show current player on left, opponent on right
            const currentPlayerUsername = (this.playerColor === 'WHITE') ? this.boardDTO.whitePlayerUsername : this.boardDTO.blackPlayerUsername;
            const opponentUsername = (this.playerColor === 'WHITE') ? this.boardDTO.blackPlayerUsername : this.boardDTO.whitePlayerUsername;
            const currentPlayerColor = (this.playerColor === 'WHITE') ? 'white' : 'black';
            const opponentColor = (this.playerColor === 'WHITE') ? 'black' : 'white';

            // Create personalized title and subtitle based on winnerId
            let popupTitle, popupSubtitle;
            
            // Debug logging
            console.log('Debug popup logic:', {
                winnerId: this.winnerId,
                userId: this.userId,
                winner: winner,
                gameStatus: this.gameStatus,
                whitePlayerId: this.boardDTO.whitePlayerId,
                blackPlayerId: this.boardDTO.blackPlayerId,
                boardDTO: this.boardDTO // Log the full boardDTO to see what's available
            });
            
            if (this.winnerId && winner !== 'Draw') {
                // We have a winner ID and it's not a draw
                if (this.winnerId === this.userId) {
                    // Current player won
                    popupTitle = 'You won';
                } else {
                    // Opponent won - show their color
                    // Determine winner color by comparing winnerId with player IDs
                    let winnerColor;
                    if (this.winnerId === this.boardDTO.whitePlayerId) {
                        winnerColor = 'White';
                    } else if (this.winnerId === this.boardDTO.blackPlayerId) {
                        winnerColor = 'Black';
                    } else {
                        // Fallback: determine based on current player's perspective
                        // If current player is White and opponent won, opponent is Black
                        // If current player is Black and opponent won, opponent is White
                        winnerColor = (this.playerColor === 'WHITE') ? 'Black' : 'White';
                    }
                    popupTitle = `${winnerColor} wins`;
                }
                
                // Create subtitle based on game type
                switch (this.gameStatus) {
                    case 'CHECKMATE':
                        popupSubtitle = 'by checkmate';
                        break;
                    case 'TIME_OUT':
                        popupSubtitle = 'by timeout';
                        break;
                    case 'RESIGNED':
                        popupSubtitle = 'by resignation';
                        break;
                    case 'STALEMATE':
                        popupSubtitle = 'by stalemate';
                        break;
                    case 'THREEFOLD_REPETITION':
                        popupSubtitle = 'by threefold repetition';
                        break;
                    case 'FIFTY_MOVE_RULE':
                        popupSubtitle = 'by fifty move rule';
                        break;
                    case 'INSUFFICIENT_MATERIAL':
                        popupSubtitle = 'by insufficient material';
                        break;
                    case 'MUTUAL_AGREEMENT':
                        popupSubtitle = 'by mutual agreement';
                        break;
                    default:
                        popupSubtitle = 'by game end';
                }
            } else {
                // Draw game or no winner ID
                if (winner === 'Draw') {
                    // Handle different types of draws
                    switch (this.gameStatus) {
                        case 'STALEMATE':
                            popupTitle = 'Draw by stalemate';
                            popupSubtitle = '';
                            break;
                        case 'THREEFOLD_REPETITION':
                            popupTitle = 'Draw by threefold repetition';
                            popupSubtitle = '';
                            break;
                        case 'FIFTY_MOVE_RULE':
                            popupTitle = 'Draw by fifty move rule';
                            popupSubtitle = '';
                            break;
                        case 'INSUFFICIENT_MATERIAL':
                            popupTitle = 'Draw by insufficient material';
                            popupSubtitle = '';
                            break;
                        case 'MUTUAL_AGREEMENT':
                            popupTitle = 'Draw by mutual agreement';
                            popupSubtitle = '';
                            break;
                        case 'DRAW':
                            popupTitle = 'Draw';
                            popupSubtitle = '';
                            break;
                        default:
                            popupTitle = 'Game Over';
                            popupSubtitle = this.gameStatus === 'TIME_OUT' ? 'by timeout' : 'by checkmate';
                    }
                } else {
                    // No winner ID but not a draw - fallback
                    popupTitle = 'Game Over';
                    popupSubtitle = this.gameStatus === 'TIME_OUT' ? 'by timeout' : 'by checkmate';
                }
            }

            this.showGameEndPopup(
                winner,
                result,
                currentPlayerUsername,  // Always current player on left
                currentPlayerColor,     // Current player's color
                opponentUsername,       // Always opponent on right
                opponentColor,          // Opponent's color
                popupTitle,             // Personalized title
                popupSubtitle           // Personalized subtitle
            );
        }
        
        const tiles = this.board.querySelectorAll('.tile');
        tiles.forEach((tile, index) => {
            // Save coordinate elements if they exist
            const fileCoord = tile.querySelector('.coordinate-file');
            const rankCoord = tile.querySelector('.coordinate-rank');
            
            // Clear tile content
            tile.innerHTML = '';
            
            // Restore coordinate elements if they existed
            if (fileCoord) tile.appendChild(fileCoord);
            if (rankCoord) tile.appendChild(rankCoord);
            
            // Check if the board has tiles property
            if (!this.boardDTO.tiles || !Array.isArray(this.boardDTO.tiles)) {
                return;
            }
            
            let tileCoordinate = index;
            
            const tileData = this.boardDTO.tiles.find(t => t.tileCoordinate === tileCoordinate);
            if (tileData && tileData.tileOccupied) {
                const piece = tileData.piece;
                if (piece) {
                    const pieceKey = piece.pieceAlliance + '_' + piece.pieceSymbol;
                    const pieceElement = document.createElement('div');
                    pieceElement.className = 'piece';
                    pieceElement.style.backgroundImage = `url('${this.pieceImages[pieceKey]}')`;
                    pieceElement.style.width = '98%';
                    pieceElement.style.height = '98%';
                    pieceElement.style.position = 'relative';
                    pieceElement.style.zIndex = '2';
                    // Add hardware acceleration for ultra-fast performance
                    pieceElement.style.willChange = 'transform';
                    pieceElement.style.backfaceVisibility = 'hidden';
                    pieceElement.style.perspective = '1000px';
                    tile.appendChild(pieceElement);
                }
            }
        });

        // Check if lastMoveData exists before trying to use it
        if (this.lastMoveData) {
            try {
                // Parse lastMoveData as a JSON object (not array)
                const lastMoveDataObj = JSON.parse(this.lastMoveData);
                const lastMoveSourceCoordinate = lastMoveDataObj.sourceCoordinate;
                const lastMoveTargetCoordinate = lastMoveDataObj.targetCoordinate;
                const lastMoveType = lastMoveDataObj.moveType;
                
                // Clear previous selections and highlights
                document.querySelector('.selected')?.classList.remove('selected');
                this.clearLegalMoves();
                document.querySelectorAll('.tile.dragover').forEach(tile => {
                    tile.classList.remove('dragover');
                });
                // Clear selection
                document.querySelector('.selected')?.classList.remove('selected');
                this.selectedSourceTile = null;
                
                // Highlight the last move with move type information
                this.highlightLastMove(lastMoveSourceCoordinate, lastMoveTargetCoordinate, lastMoveType);
            } catch (e) {
                // Fallback: clear selections without highlighting
                document.querySelector('.selected')?.classList.remove('selected');
                this.clearLegalMoves();
                document.querySelectorAll('.tile.dragover').forEach(tile => {
                    tile.classList.remove('dragover');
                });
                document.querySelector('.selected')?.classList.remove('selected');
                this.selectedSourceTile = null;
            }
        } else {
            // If no last move data, just clear selections
            document.querySelector('.selected')?.classList.remove('selected');
            this.clearLegalMoves();
            document.querySelectorAll('.tile.dragover').forEach(tile => {
                tile.classList.remove('dragover');
            });
            // Clear selection
            document.querySelector('.selected')?.classList.remove('selected');
            this.selectedSourceTile = null;
        }
    }

    /**
     * Highlights the last move made on the chess board
     * @param {number} sourcePos - The source coordinate of the move (0-63)
     * @param {number} targetPos - The target coordinate of the move (0-63)
     * @param {string} moveType - The type of move (NORMAL, CAPTURE, CHECK, etc.)
     */
    highlightLastMove(sourcePos, targetPos, moveType) {
        // Clear previous move highlights to avoid conflicts
        document.querySelectorAll('.last-move-source, .last-move-target, .last-move-capture').forEach(tile => {
            tile.classList.remove('last-move-source', 'last-move-target', 'last-move-capture');
        });

        // Find the source and target tiles using their data-position attribute
        const sourceTile = this.board.querySelector(`.tile[data-position='${sourcePos}']`);
        const targetTile = this.board.querySelector(`.tile[data-position='${targetPos}']`);
        
        // Add highlighting to source tile (where the piece moved from)
        if (sourceTile) {
            sourceTile.classList.add('last-move-source');
        }
        
        // Add highlighting to target tile (where the piece moved to)
        if (targetTile) {
            targetTile.classList.add('last-move-target');
        }
        
        // Add special red highlighting for capture moves
        // This makes it easy to distinguish captures from normal moves
        if (moveType === 'CAPTURE' && targetTile) {
            targetTile.classList.add('last-move-capture');
        }

        if(moveType === 'CHECK') {
            // Removed king in check highlighting
        }
    }

    /**
     * Highlights the king's square with orange color when the king is in check
     */
    // Removed highlightKingInCheck function

    /**
     * Clears the king check highlighting
     */
    // Removed clearKingCheckHighlight function

    showGameEndPopup(winner, result, currentPlayerUsername, currentPlayerColor, opponentUsername, opponentColor, title, subtitle) {
        // Remove existing popup if any
        let existing = document.getElementById('game-end-popup');
        if (existing) existing.remove();

        // Create popup
        const popup = document.createElement('div');
        popup.id = 'game-end-popup';
        popup.innerHTML = `
            <button class="popup-close" id="close-game-end-popup" title="Close">&#10005;</button>
            <div class="popup-title">${title}</div>
            <div class="popup-subtitle">${subtitle}</div>
            <div class="popup-players">
                <div class="popup-player ${winner === currentPlayerUsername ? 'popup-winner' : ''}">
                    <div class="popup-color" style="background-color: ${currentPlayerColor};"></div>
                    <div class="popup-username">${currentPlayerUsername}</div>
                </div>
                <div class="popup-result-center">${result}</div>
                <div class="popup-player ${winner === opponentUsername ? 'popup-winner' : ''}">
                    <div class="popup-color" style="background-color: ${opponentColor};"></div>
                    <div class="popup-username">${opponentUsername}</div>
                </div>
            </div>
            <div class="popup-actions">
                <button id="offer-rematch-btn" class="popup-action-btn btn-rematch">
                    <i class="fas fa-redo"></i>
                    Offer Rematch
                </button>
                <button id="new-game-btn" class="popup-action-btn btn-new-game">
                    <i class="fas fa-plus"></i>
                    New Game
                </button>
            </div>
        `;
        document.body.appendChild(popup);

        // Close button
        document.getElementById('close-game-end-popup').onclick = () => popup.remove();

        // Offer Rematch button
        document.getElementById('offer-rematch-btn').onclick = () => this.handleOfferRematch();

        // New Game button
        document.getElementById('new-game-btn').onclick = () => this.handleNewGame();

        // Add styles if not present
        if (!document.getElementById('game-end-popup-style')) {
            const style = document.createElement('style');
            style.id = 'game-end-popup-style';
            style.innerHTML = `
            #game-end-popup {
                position: fixed;
                top: 50%; left: 50%;
                transform: translate(-50%, -50%);
                background: rgba(40, 30, 60, 0.97);
                color: #fff;
                padding: 2.5rem 2.5rem 2rem 2.5rem;
                border-radius: 18px;
                box-shadow: 0 8px 32px rgba(0,0,0,0.45);
                z-index: 99999;
                text-align: center;
                min-width: 340px;
                max-width: 95vw;
                font-family: 'Poppins', sans-serif;
                border: 2px solid var(--color-accent, #ffd700);
                backdrop-filter: blur(6px);
                animation: popupAppear 0.4s cubic-bezier(.68,-0.55,.27,1.55);
            }
            #game-end-popup .popup-close {
                background: none;
                color: #fff;
                border: none;
                border-radius: 50%;
                width: 2.2rem;
                height: 2.2rem;
                font-size: 1.5rem;
                cursor: pointer;
                position: absolute;
                top: 1rem;
                right: 1rem;
                transition: background 0.2s, color 0.2s;
            }
            #game-end-popup .popup-close:hover {
                background: #fff;
                color: #222;
            }
            #game-end-popup .popup-title {
                font-size: 2.2rem;
                font-weight: 800;
                margin-bottom: 0.2rem;
                letter-spacing: 1px;
            }
            #game-end-popup .popup-subtitle {
                font-size: 1.1rem;
                color: #ffd700;
                margin-bottom: 1.2rem;
                font-weight: 500;
            }
            #game-end-popup .popup-players {
                display: flex;
                align-items: center;
                justify-content: center;
                gap: 2.2rem;
                margin-top: 1.2rem;
                margin-bottom: 2rem;
            }
            #game-end-popup .popup-player {
                display: flex;
                flex-direction: column;
                align-items: center;
                min-width: 90px;
            }
            #game-end-popup .popup-color {
                width: 24px;
                height: 24px;
                border-radius: 50%;
                margin-bottom: 0.5rem;
                border: 2px solid #fff;
            }
            #game-end-popup .popup-color[style*="white"] {
                background-color: #fff !important;
                border: 2px solid #333;
            }
            #game-end-popup .popup-color[style*="black"] {
                background-color: #333 !important;
                border: 2px solid #fff;
            }
            #game-end-popup .popup-color[style*="draw"] {
                background: linear-gradient(45deg, #fff 50%, #333 50%);
                border: 2px solid #fff;
            }
            #game-end-popup .popup-winner .popup-color {
                border: 2px solid var(--color-accent, #ffd700);
                box-shadow: 0 0 8px 2px var(--color-accent, #ffd700);
            }
            #game-end-popup .popup-username {
                font-size: 1.1rem;
                font-weight: 600;
                margin-top: 0.2rem;
                color: #fff;
                text-shadow: 0 1px 2px #0008;
            }
            #game-end-popup .popup-winner .popup-username {
                color: var(--color-accent, #ffd700);
            }
            #game-end-popup .popup-result-center {
                font-size: 2rem;
                font-weight: 700;
                color: #fff;
                margin: 0 1.2rem;
                align-self: center;
            }
            #game-end-popup .popup-actions {
                display: flex;
                gap: 1rem;
                justify-content: center;
                margin-top: 1.5rem;
            }
            #game-end-popup .popup-action-btn {
                padding: 12px 20px;
                border: none;
                border-radius: 8px;
                font-size: 1rem;
                font-weight: 600;
                cursor: pointer;
                transition: all 0.3s ease;
                display: flex;
                align-items: center;
                gap: 8px;
                min-width: 140px;
                justify-content: center;
            }
            #game-end-popup .btn-rematch {
                background: linear-gradient(135deg, #28a745, #20c997);
                color: white;
            }
            #game-end-popup .btn-rematch:hover {
                background: linear-gradient(135deg, #20c997, #17a2b8);
                transform: translateY(-2px);
                box-shadow: 0 4px 12px rgba(40, 167, 69, 0.3);
            }
            #game-end-popup .btn-new-game {
                background: linear-gradient(135deg, #6b46c1, #553c9a);
                color: white;
            }
            #game-end-popup .btn-new-game:hover {
                background: linear-gradient(135deg, #553c9a, #44337a);
                transform: translateY(-2px);
                box-shadow: 0 4px 12px rgba(107, 70, 193, 0.3);
            }
            @keyframes popupAppear {
                0% { transform: translate(-50%, -50%) scale(0.7); opacity: 0; }
                100% { transform: translate(-50%, -50%) scale(1); opacity: 1; }
            }
            
            @media (max-width: 768px) {
                #game-end-popup .popup-actions {
                    flex-direction: column;
                    gap: 0.8rem;
                }
                #game-end-popup .popup-action-btn {
                    width: 100%;
                    padding: 14px 20px;
                }
            }
            `;
            document.head.appendChild(style);
        }
    }

    // Update the playSound method with better debugging
    playSound(moveType) {
        if (this.sounds[moveType]) {
            this.sounds[moveType].play().catch(e => {
                // Silent error handling for speed
            });
        }
    }
    
    // Position Navigation Methods
    
    /**
     * Load all game positions for the current game
     * This method fetches the complete move history from the server
     */
    async loadGamePositions() {
        try {
            // Fetch all game positions from the server
            const response = await fetch(`/api/games/${this.gameId}/history`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            });
            
            if (!response.ok) {
                return;
            }
        
            // Parse the response to get all positions
            this.gamePositions = await response.json();
            
            // Store the latest position for comparison
            if (this.gamePositions.length > 0) {
                this.latestPositionBoardDTO = this.gamePositions[this.gamePositions.length - 1].board;
            }
            
            // Update the moves list display with algebraic notation
            this.updateMovesList();
            
            // Update navigation button states (this will also set viewing mode)
            this.updateNavigationButtons();
            
            // Update viewing mode status
            this.updateViewingModeStatus(null);
            
        } catch (error) {
            // Silent error handling for speed
        }
    }
    
    /**
     * Navigate to the previous position in the game history
     * This method moves backward through the move history
     * Note: Navigation excludes the last position - only shuffles through historical positions
     */
    async navigateToPreviousPosition() {
        // If we're at the latest position (index -1), go to the second-to-last historical position
        if (this.currentPositionIndex === -1) {
            if (this.gamePositions.length === 0) {
                return;
            }
            // Go to the second-to-last historical position (exclude the last position from shuffle cycle)
            this.currentPositionIndex = this.gamePositions.length - 2;
        } else if (this.currentPositionIndex <= 0) {
            // If we're at the initial position, we can't go back further
            return;
        } else {
            // Move to the previous position
            this.currentPositionIndex--;
        }
        
        // Load and display the position
        await this.displayPosition(this.currentPositionIndex);
        
        // Update navigation button states
        this.updateNavigationButtons();
    }
    
    /**
     * Navigate to the initial position (move 0)
     * This method moves to the very beginning of the game
     */
    async navigateToInitialPosition() {
        if (this.gamePositions.length === 0) {
            return;
        }
        
        // Move to the initial position (index 0)
        this.currentPositionIndex = 0;
        
        // Load and display the position
        await this.displayPosition(this.currentPositionIndex);
        
        // Update navigation button states
        this.updateNavigationButtons();
    }
    
    /**
     * Navigate to the next position in the game history
     * This method moves forward through the move history
     * Note: Navigation excludes the last position - only shuffles through historical positions
     */
    async navigateToNextPosition() {
        // If we're at the latest position, we can't go forward further
        if (this.currentPositionIndex === -1) {
            return;
        }
        
        // If we're at the second-to-last historical position, go to the latest position (current playable state)
        // This skips the last position in the array, excluding it from the shuffle cycle
        if (this.currentPositionIndex >= this.gamePositions.length - 2) {
            await this.returnToLatestPosition();
            return;
        }
        
        // Move to the next position
        this.currentPositionIndex++;
        
        // Load and display the position
        await this.displayPosition(this.currentPositionIndex);
        
        // Update navigation button states
        this.updateNavigationButtons();
    }
    
    /**
     * Display a specific position on the board
     * This method updates the board to show the position at the given index
     * Note: This only updates the display - timer calculations use latestGameState
     * @param {number} positionIndex - The index of the position to display
     */
    async displayPosition(positionIndex) {
        // Validate position index
        if (positionIndex < 0 || positionIndex >= this.gamePositions.length) {
            return;
        }
        
        // Get the position data
        const position = this.gamePositions[positionIndex];
        
        // Update the board DTO with the position data (for display only)
        // Timer calculations will use latestGameState, not this historical data
        this.boardDTO = position.board;
        
        // Update the board display
        this.updateBoard();
        
        // Update navigation buttons (this will also set viewing mode)
        this.updateNavigationButtons();
        
        // Update the status to show we're viewing a position
        this.updateViewingModeStatus(position);
    }
    
    /**
     * Return to the latest position (current game state)
     * This method exits viewing mode and returns to the live game
     */
    async returnToLatestPosition() {
        // Reset to latest position
        this.currentPositionIndex = -1;
        
        // Fetch the current game state
        await this.fetchGame();
        
        // Update navigation button states (this will also set viewing mode to false)
        this.updateNavigationButtons();
        
        // Update the status to show we're back to the live game
        this.updateViewingModeStatus(null);
    }
    
    /**
     * Update the navigation button states based on current position
     * This method enables/disables the left and right arrow buttons
     * Note: Navigation excludes the last position - only shuffles through historical positions
     */
    updateNavigationButtons() {
        if (!this.prevPositionBtn || !this.nextPositionBtn) {
            return;
        }
        
        // If no positions available, disable all navigation buttons
        if (this.gamePositions.length === 0) {
            this.prevPositionBtn.disabled = true;
            this.nextPositionBtn.disabled = true;
            if (this.initialPositionBtn) this.initialPositionBtn.disabled = true;
            if (this.latestPositionBtn) this.latestPositionBtn.disabled = true;
            return;
        }
        
        // Previous button logic:
        // - When at latest position (index -1): can go back to second-to-last historical position
        // - When at any historical position: can go back to previous historical position
        // - When at initial position (index 0): cannot go back further
        const canGoBack = this.currentPositionIndex > 0 || this.currentPositionIndex === -1;
        this.prevPositionBtn.disabled = !canGoBack;
        
        // Next button logic:
        // - When at latest position (index -1): cannot go forward (already at current playable state)
        // - When at any historical position: can go forward to next historical position
        // - When at second-to-last historical position: can go forward to current playable state (skipping last position)
        const isAtLatestBoard = this.currentPositionIndex === -1;
        const canGoForward = !isAtLatestBoard;
        this.nextPositionBtn.disabled = !canGoForward;
        
        // Initial position button logic:
        // - Always enabled if there are positions available
        // - When at initial position: can be clicked but won't change anything
        if (this.initialPositionBtn) {
            this.initialPositionBtn.disabled = false;
        }
        
        // Latest position button logic:
        // - Always enabled if there are positions available
        // - When at latest position: can be clicked but won't change anything
        if (this.latestPositionBtn) {
            this.latestPositionBtn.disabled = false;
        }
        
        // Update viewing mode based on whether we're at the latest board
        this.isViewingMode = !isAtLatestBoard;
    }
    
    /**
     * Update the status display to show viewing mode information
     * This method updates the status text when browsing positions
     * @param {Object} position - The current position being viewed (null if not in viewing mode)
     */
    updateViewingModeStatus(position) {
        // Find the viewing indicator element (create if it doesn't exist)
        let viewingIndicator = document.querySelector('.viewing-mode-indicator');
        if (!viewingIndicator) {
            viewingIndicator = document.createElement('div');
            viewingIndicator.className = 'viewing-mode-indicator';
            viewingIndicator.textContent = 'VIEWING';
            viewingIndicator.style.display = 'none';
            
            // Add it next to the timer in the current player info
            const playerTimer = document.querySelector('.current-player-info .player-timer');
            if (playerTimer) {
                playerTimer.parentNode.insertBefore(viewingIndicator, playerTimer.nextSibling);
            }
        }
        
        if (this.isViewingMode && position) {
            // Show viewing mode indicator
            viewingIndicator.style.display = 'block';
            
            // Update status text
            this.statusElement.textContent = `Viewing: ${position.moveNotation} (Move ${position.moveNumber})`;
            this.statusElement.classList.add('viewing-mode');
        } else {
            // Hide viewing mode indicator
            viewingIndicator.style.display = 'none';
            
            // Remove viewing mode status
            this.statusElement.classList.remove('viewing-mode');
            
            // Status will be updated by the normal updateBoard method
        }
    }
    
    /**
     * Check if the current position is the latest position
     * This method compares the current board with the latest position
     * @returns {boolean} True if we're viewing the latest position
     */
    isAtLatestPosition() {
        return this.currentPositionIndex === -1;
    }
    
    /**
     * Setup event listeners for position navigation
     * This method binds click handlers to the navigation buttons
     */
    setupPositionNavigationListeners() {
        // Force enable buttons temporarily for testing
        if (this.prevPositionBtn) {
            this.prevPositionBtn.disabled = false;
        }
        if (this.nextPositionBtn) {
            this.nextPositionBtn.disabled = false;
        }
        
        // Bind click handlers to navigation buttons
        if (this.prevPositionBtn) {
            this.prevPositionBtn.addEventListener('click', (e) => {
                e.preventDefault();
                this.navigateToPreviousPosition();
            });
        }
        
        if (this.nextPositionBtn) {
            this.nextPositionBtn.addEventListener('click', (e) => {
                e.preventDefault();
                this.navigateToNextPosition();
            });
        }
        
        // Bind click handlers to initial and latest position buttons
        if (this.initialPositionBtn) {
            this.initialPositionBtn.addEventListener('click', (e) => {
                e.preventDefault();
                this.navigateToInitialPosition();
            });
        }
        
        if (this.latestPositionBtn) {
            this.latestPositionBtn.addEventListener('click', (e) => {
                e.preventDefault();
                this.returnToLatestPosition();
            });
        }
        
        // Add keyboard navigation support - works like clicking the buttons
        document.addEventListener('keydown', (e) => {
            switch (e.key) {
                case 'ArrowLeft':
                    e.preventDefault();
                    // Only navigate if previous button is enabled
                    if (this.prevPositionBtn && !this.prevPositionBtn.disabled) {
                        this.navigateToPreviousPosition();
                    }
                    break;
                case 'ArrowRight':
                    e.preventDefault();
                    // Only navigate if next button is enabled
                    if (this.nextPositionBtn && !this.nextPositionBtn.disabled) {
                        this.navigateToNextPosition();
                    }
                    break;
                case 'Escape':
                    e.preventDefault();
                    this.returnToLatestPosition();
                    break;
                case 'Home':
                    e.preventDefault();
                    // Go to initial position
                    this.navigateToInitialPosition();
                    break;
                case 'End':
                    e.preventDefault();
                    // Go to latest position (current playable state)
                    this.returnToLatestPosition();
                    break;
            }
        });
    }

    /**
     * Update the moves list display with algebraic notation
     * This method populates the move history section with the current game's moves
     */
    updateMovesList() {
        // Get the moves list container
        const movesListContainer = document.getElementById('moves-list');
        if (!movesListContainer) {
            return;
        }
        
        // Clear the current moves list
        movesListContainer.innerHTML = '';
        
        // If no game positions loaded, show empty state
        if (!this.gamePositions || this.gamePositions.length === 0) {
            movesListContainer.innerHTML = '<div class="no-moves">No moves yet</div>';
            return;
        }
        
        // Create moves display in pairs (White and Black moves together)
        let movesHTML = '';
        let moveNumber = 1;
        let whiteMove = '';
        let blackMove = '';
        
        // Loop through positions (skip the initial position at index 0)
        for (let i = 1; i < this.gamePositions.length; i++) {
            const position = this.gamePositions[i];
            
            // Only process positions that have move notation (actual moves, not initial position)
            if (position.moveNotation) {
                // Determine if this is a White or Black move based on position index
                // First move (i=1) is White, second (i=2) is Black, third (i=3) is White, etc.
                const isWhiteMove = (i % 2 === 1); // Odd indices (1, 3, 5...) are White moves
                
                if (isWhiteMove) {
                    // This is a White move - store it and wait for Black's move
                    whiteMove = position.moveNotation;
                } else {
                    // This is a Black move - now we have both moves, create the entry
                    blackMove = position.moveNotation;
                    
                    movesHTML += `
                        <div class="move-entry" data-move-number="${moveNumber}">
                            <span class="move-number">${moveNumber}.</span>
                            <span class="move-notation">${whiteMove} ${blackMove}</span>
                        </div>
                    `;
                    moveNumber++;
                    whiteMove = '';
                    blackMove = '';
                }
            }
        }
        
        // Handle the case where we have a White move but no Black move (game ended on White's move)
        if (whiteMove && !blackMove) {
            movesHTML += `
                <div class="move-entry" data-move-number="${moveNumber}">
                    <span class="move-number">${moveNumber}.</span>
                    <span class="move-notation">${whiteMove}</span>
                </div>
            `;
        }
        
        // Update the moves list
        movesListContainer.innerHTML = movesHTML;
        
        // Add click handlers to move entries for navigation
        this.setupMoveEntryClickHandlers();
    }
    
    /**
     * Setup click handlers for move entries to allow navigation
     * Clicking on a move will navigate to that position
     */
    setupMoveEntryClickHandlers() {
        const moveEntries = document.querySelectorAll('.move-entry');
        moveEntries.forEach((entry, index) => {
            entry.addEventListener('click', () => {
                // Navigate to the position after this move pair is completed
                // Each move pair contains 2 moves (White + Black), so we need to calculate the correct position index
                // Move pair 1 (1.e4 e5) should navigate to position after both moves (index 2)
                // Move pair 2 (2.Nf3 Nc6) should navigate to position after both moves (index 4)
                // etc.
                const positionIndex = (index + 1) * 2; // Convert move pair index to position index
                this.navigateToPosition(positionIndex);
            });
        });
    }
    
    /**
     * Navigate to a specific position by index
     * @param {number} positionIndex - The index of the position to navigate to
     */
    async navigateToPosition(positionIndex) {
        if (positionIndex < 0 || positionIndex >= this.gamePositions.length) {
            return;
        }
        
        this.currentPositionIndex = positionIndex;
        await this.displayPosition(positionIndex);
        this.updateNavigationButtons();
    }

    /**
     * Execute a move with immediate client-side visual feedback
     * This method moves the piece instantly on the frontend for chess.com-like experience
     * @param {number} sourceCoordinate - The source coordinate of the move
     * @param {number} targetCoordinate - The target coordinate of the move
     */
    async executeClientSideMove(sourceCoordinate, targetCoordinate) {
        // Store original board state for potential rollback
        const originalBoardDTO = JSON.parse(JSON.stringify(this.boardDTO));
        
        // Stop timer updates during client-side move to prevent showing old values
        this.stopTimer();
        this.isUpdatingFromServer = true;
        
        // Immediately update the board visually for instant feedback
        this.performClientSideMove(sourceCoordinate, targetCoordinate);
        
        // Clear selection and legal moves immediately
        this.clearLegalMoves();
        document.querySelector('.selected')?.classList.remove('selected');
        this.selectedSourceTile = null;
        
        // Send move to server for validation
        const pathParts = window.location.pathname.split('/');
        this.gameId = pathParts[pathParts.length - 1];
        
        try {
            const response = await fetch(`/api/games/${this.gameId}/move`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    gameId: this.gameId,
                    sourceCoordinate: sourceCoordinate,
                    targetCoordinate: targetCoordinate
                })
            });
            
            if (response.ok) {
                const responseText = await response.text();
                if (responseText) {
                    try {
                        const moveResult = JSON.parse(responseText);
                        if (moveResult.board) {
                            // Update with server response (this will include any server-side changes)
                            if (typeof moveResult.board === 'string') {
                                this.boardDTO = JSON.parse(moveResult.board);
                            } else {
                                this.boardDTO = moveResult.board;
                            }
                            this.updateBoard();
                        }
                    } catch (e) {
                        // If parsing fails, rollback to original state
                        this.boardDTO = originalBoardDTO;
                        this.updateBoard();
                    }
                }
            } else {
                // If server rejects the move, rollback to original state
                this.boardDTO = originalBoardDTO;
                this.updateBoard();
            }
        } catch (error) {
            // If network error, rollback to original state
            this.boardDTO = originalBoardDTO;
            this.updateBoard();
        }
        
        // Allow timer updates to resume (WebSocket will handle the actual timer restart)
        this.isUpdatingFromServer = false;
    }
    
    /**
     * Perform the visual move on the client side immediately
     * @param {number} sourceCoordinate - The source coordinate of the move
     * @param {number} targetCoordinate - The target coordinate of the move
     */
    performClientSideMove(sourceCoordinate, targetCoordinate) {
        // Find the source and target tiles
        const sourceTile = this.board.querySelector(`.tile[data-position='${sourceCoordinate}']`);
        const targetTile = this.board.querySelector(`.tile[data-position='${targetCoordinate}']`);
        
        if (!sourceTile || !targetTile) return;
        
        // Get the piece from source tile
        const piece = sourceTile.querySelector('.piece');
        if (!piece) return;
        
        // Remove any existing piece from target tile (capture)
        const existingPiece = targetTile.querySelector('.piece');
        if (existingPiece) {
            existingPiece.style.opacity = '0'; // Make captured piece invisible (faster than DOM removal)
        }
        
        // Move the piece visually to the target tile
        targetTile.appendChild(piece);
        
        // Update the board DTO to reflect the move
        const sourceTileData = this.boardDTO.tiles.find(t => t.tileCoordinate === sourceCoordinate);
        const targetTileData = this.boardDTO.tiles.find(t => t.tileCoordinate === targetCoordinate);
        
        if (sourceTileData && targetTileData) {
            // Move piece data
            targetTileData.piece = sourceTileData.piece;
            targetTileData.tileOccupied = true;
            
            // Clear source tile data
            sourceTileData.piece = null;
            sourceTileData.tileOccupied = false;
            
            // Update current player (switch turns)
            if (this.boardDTO.currentPlayer.alliance === 'WHITE') {
                this.boardDTO.currentPlayer.alliance = 'BLACK';
            } else {
                this.boardDTO.currentPlayer.alliance = 'WHITE';
            }
            
            // Update player turn status
            this.isPlayerTurn = false;
            
            // Update status text
            if (this.boardDTO.currentPlayer.alliance === this.playerColor) {
                this.statusElement.textContent = 'Your turn to move';
                this.statusElement.classList.add('your-turn');
                this.isPlayerTurn = true;
            } else {
                this.statusElement.textContent = `Waiting for ${this.boardDTO.currentPlayer.alliance.toLowerCase()} to move`;
                this.statusElement.classList.remove('your-turn');
                this.isPlayerTurn = false;
            }
        }
    }

    // Chat Modal Methods
    toggleChatModal() {
        const isExpanding = !this.chatModal.classList.contains('show');
        const chatSection = document.querySelector('.game-chat-section');
        
        if (isExpanding) {
            // Sync existing messages from small chat to modal when expanding
            this.syncChatMessages();
            
            // Focus on the input field after a short delay
            setTimeout(() => {
                this.chatModalInput.focus();
            }, 100);
            
            // Update tooltip to show "Click to shrink"
            if (chatSection) {
                chatSection.style.setProperty('--tooltip-text', '"Click to shrink"');
            }
        } else {
            // Update tooltip to show "Click to enlarge"
            if (chatSection) {
                chatSection.style.setProperty('--tooltip-text', '"Click to enlarge"');
            }
        }
        
        this.chatModal.classList.toggle('show');
    }
    
    syncChatMessages() {
        // Clear existing modal messages
        this.chatModalMessages.innerHTML = '';
        
        // Copy all messages from small chat to modal
        const smallChatMessages = this.chatMessages.querySelectorAll('.chat-message');
        smallChatMessages.forEach(smallMessage => {
            const modalMessageDiv = document.createElement('div');
            modalMessageDiv.className = 'chat-modal-message';
            
            // Clone the content from small message
            const sender = smallMessage.querySelector('.sender')?.textContent || '';
            const content = smallMessage.querySelector('.content')?.textContent || '';
            const timestamp = smallMessage.querySelector('.timestamp')?.textContent || '';
            
            const modalSenderDiv = document.createElement('div');
            modalSenderDiv.className = 'sender';
            modalSenderDiv.textContent = sender;
            
            const modalContentDiv = document.createElement('div');
            modalContentDiv.className = 'content';
            modalContentDiv.textContent = content;
            
            const modalTimestampDiv = document.createElement('div');
            modalTimestampDiv.className = 'timestamp';
            modalTimestampDiv.textContent = timestamp;
            
            modalMessageDiv.appendChild(modalSenderDiv);
            modalMessageDiv.appendChild(modalContentDiv);
            modalMessageDiv.appendChild(modalTimestampDiv);
            
            this.chatModalMessages.appendChild(modalMessageDiv);
        });
        
        // Scroll to bottom
        this.chatModalMessages.scrollTop = this.chatModalMessages.scrollHeight;
    }

    sendChatMessageFromModal() {
        const message = this.chatModalInput.value.trim();
        if (!message || !this.stompClient || !this.stompClient.connected) return;
        
        const chatMessage = {
            gameId: this.gameId,
            message: message,
            sender: this.username,
            timestamp: Date.now()
        };
        
        this.stompClient.send('/app/chat/' + this.gameId, {}, JSON.stringify(chatMessage));
        this.chatModalInput.value = '';
        // Don't close the modal - let user continue chatting
    }
}

// Initialize the game when the page loads
document.addEventListener('DOMContentLoaded', () => {
    const gameBoard = document.getElementById('game-board');
    if (gameBoard) {
        // Initialize the chess game
        window.chessGame = new ChessGame();
        
        // Get game ID from URL path
        const pathParts = window.location.pathname.split('/');
        const gameId = pathParts[pathParts.length - 1];
        
        if (gameId) {
            // Set the game ID in the chess game instance
            window.chessGame.gameId = gameId;
            // Start the game automatically
            if (window.chessGame) {
                window.chessGame.fetchGame();
            }
        }
    }
}); 