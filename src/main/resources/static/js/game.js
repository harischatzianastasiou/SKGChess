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
        
        // Position navigation properties
        this.isViewingMode = false; // Track if we're in viewing mode (browsing positions)
        this.currentPositionIndex = -1; // Current position index (-1 = latest position)
        this.gamePositions = []; // Array of all game positions
        this.latestPositionBoardDTO = null; // Store the latest position for comparison
        
        // Navigation button elements
        this.prevPositionBtn = document.getElementById('prev-position-btn');
        this.nextPositionBtn = document.getElementById('next-position-btn');
        
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
        console.log('Game ID from URL:', this.gameId);
        
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
        
        // Bind chat event handlers
        this.chatInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                this.sendChatMessage();
            }
        });
        this.sendMessageBtn.addEventListener('click', () => this.sendChatMessage());
        
        // Initialize the game asynchronously
        this.initializeGame();
        
        // Add sound effects
        console.log('[Sound Debug] Initializing sound effects...');
        this.sounds = {
            move: new Audio('/audio/Move.wav'),
            capture: new Audio('/audio/Capture.wav'),
            check: new Audio('/audio/Check.wav')
        };
        
        // Preload sounds with comprehensive debugging
        Object.entries(this.sounds).forEach(([type, sound]) => {
            console.log(`[Sound Debug] Initializing ${type} sound from ${sound.src}`);
            
            // Add error handler first
            sound.addEventListener('error', (e) => {
                console.error(`[Sound Debug] Error loading ${type} sound:`, {
                    error: e,
                    errorCode: sound.error?.code,
                    errorMessage: sound.error?.message,
                    readyState: sound.readyState,
                    src: sound.src
                });
            });
            
            // Add success handlers
            sound.addEventListener('loadeddata', () => {
                console.log(`[Sound Debug] ${type} sound loaded successfully:`, {
                    duration: sound.duration,
                    readyState: sound.readyState,
                    src: sound.src
                });
            });
            
            sound.addEventListener('canplaythrough', () => {
                console.log(`[Sound Debug] ${type} sound can play through:`, {
                    duration: sound.duration,
                    readyState: sound.readyState,
                    src: sound.src
                });
            });
            
            // Try to load the sound
            try {
                sound.load();
                console.log(`[Sound Debug] Load called for ${type} sound`);
            } catch (e) {
                console.error(`[Sound Debug] Error calling load() for ${type} sound:`, e);
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
        console.log('User ID fetched:', this.userId);
        
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
        
        if (this.timerInterval) {
            clearInterval(this.timerInterval);
        }
        
        this.timerInterval = setInterval(() => {
            this.updateTimerDisplay();
        }, 1000); // Update every second
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
        if (!this.lastMoveAt || !this.whiteTimeLeftSeconds || !this.blackTimeLeftSeconds) {
            return;
        }

        // Don't update timer if game has ended (but allow CHECK status for timeout detection)
        if (this.gameStatus !== 'IN_PROGRESS' && this.gameStatus !== 'CHECK') {
            return;
        }

        // Use server-synchronized time for accurate calculations
        const now = new Date();
        const serverAdjustedNow = new Date(now.getTime() + (this.serverTimeOffset || 0)); // Apply server time offset
        const lastMoveTime = new Date(this.lastMoveAt);
        const elapsedSeconds = Math.floor((serverAdjustedNow - lastMoveTime) / 1000);

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
                opponentTimer.textContent = this.formatTime(blackTimeLeft);
                currentPlayerTimer.textContent = this.formatTime(whiteTimeLeft);
                
                // Add low time warning
                opponentTimer.className = blackTimeLeft <= 10 ? 'player-timer low-time' : 'player-timer';
                currentPlayerTimer.className = whiteTimeLeft <= 10 ? 'player-timer low-time' : 'player-timer';
            } else {
                // Current player is black, so opponent is white
                opponentTimer.textContent = this.formatTime(whiteTimeLeft);
                currentPlayerTimer.textContent = this.formatTime(blackTimeLeft);
                
                // Add low time warning
                opponentTimer.className = whiteTimeLeft <= 10 ? 'player-timer low-time' : 'player-timer';
                currentPlayerTimer.className = blackTimeLeft <= 10 ? 'player-timer low-time' : 'player-timer';
            }
        }

        // Check for timeout - only check the player whose turn it is
        // Allow timeout detection even when game is in CHECK status
        if (currentPlayerAlliance) {
            if ((currentPlayerAlliance === 'WHITE' && whiteTimeLeft <= 0) || 
                (currentPlayerAlliance === 'BLACK' && blackTimeLeft <= 0)) {
                console.log('Timeout detected! Current player alliance:', currentPlayerAlliance, 'Game status:', this.gameStatus);
                this.handleTimeout();
            }
        }
    }

    // Format time as MM:SS
    formatTime(seconds) {
        const minutes = Math.floor(seconds / 60);
        const remainingSeconds = seconds % 60;
        return `${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}`;
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
                console.log('Timeout handled successfully');
                // Fetch the updated game state to get the new status and winner
                await this.fetchGame();
                
                // Update the board to show the end game popup
                this.updateBoard();
            } else {
                console.error('Failed to handle timeout');
            }
        } catch (error) {
            console.error('Error handling timeout:', error);
        }
    }

    initializeBoard() {
        console.log('Setting up board...');
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
    }

    // Function to fetch user ID by username
    async fetchUserIdByUsername(username) {
        try {
            const response = await fetch(`/api/users/${username}`);
            if (!response.ok) {
                throw new Error('User not found');
            }
            const user = await response.json();
            console.log("User data:", user);
            if (!user || !user.id) {
                throw new Error('User ID not found in response');
            }
            console.log("User ID:", user.id);
            return user.id;
        } catch (error) {
            console.error("Error fetching user ID:", error);
            return null;
        }
    }

    async fetchGame() {
        console.log('Fetching game...');

        const response = await fetch(`/api/games/${this.gameId}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        if (!response.ok) {
            const errorText = await response.text();
            console.error('Fetching game failed:', response.status, errorText);
            throw new Error('Fetching game failed');
        }
        
        // Get the response data
        const gameData = await response.json();
        console.log('Game data received:', gameData);
        
        // Sync client time with server time for accurate timer calculations
        if (gameData.serverTime) {
            // Calculate time offset between client and server (in milliseconds)
            const serverTime = new Date(gameData.serverTime);
            const clientTime = new Date();
            this.serverTimeOffset = serverTime.getTime() - clientTime.getTime();
            console.log('Server time sync - Server:', serverTime, 'Client:', clientTime, 'Offset (ms):', this.serverTimeOffset);
        }
        
        // Check if the board data is directly in the response or nested
        if (gameData.board) {
            // Check if board is a string (JSON) that needs to be parsed
            if (typeof gameData.board === 'string') {
                try {
                    this.boardDTO = JSON.parse(gameData.board);
                } catch (e) {
                    console.error('Error parsing board JSON:', e);
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
        
        // Debug the board data structure
        console.log('Board DTO structure:', this.boardDTO);
        console.log('Has tiles property:', this.boardDTO.hasOwnProperty('tiles'));
        console.log('Tiles is array:', Array.isArray(this.boardDTO.tiles));
        
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
            console.log('New game started - resetting game end popup flag');
            this.gameEndPopupShown = false;
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
    }

    connectWebSocket() {
        console.log('Connecting to WebSocket...');
        console.log('Username for WebSocket connection:', this.username);
        
        // Make sure we have a valid gameId before attempting to connect
        if (!this.gameId) {
            console.error('Cannot connect to WebSocket: gameId is undefined');
            return;
        }
        
        // Make sure we have a valid userId before attempting to connect
        if (!this.userId) {
            console.error('Cannot connect to WebSocket: userId is undefined');
            return;
        }
        
        const socket = new SockJS('/chess-websocket');
        this.stompClient = Stomp.over(socket);
        
        this.stompClient.debug = function(str) {
            console.log('STOMP: ' + str);
        };
    
        this.stompClient.connect({}, 
            (frame) => {
                console.log('Connected to WebSocket: ' + frame);
                this.reconnectAttempts = 0;
                
                // Subscribe to game moves
                if (this.stompClient && this.stompClient.connected) {
                    this.stompClient.subscribe('/topic/game/' + this.gameId, async (message) => {
                        console.log('Received WebSocket message for game ID:', this.gameId, 'Message:', message.body);
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
                                
                                // Start timer if game has started and timer is enabled
                                if ((this.gameStatus === 'IN_PROGRESS' || this.gameStatus === 'CHECK') && this.timeControlMinutes) {
                                    this.startTimer();
                                }
                                
                                // Update the status based on player color
                                if (this.playerColor === 'WHITE') {
                                    this.statusElement.textContent = 'Your turn';
                                    this.isPlayerTurn = true;
                                } else {
                                    this.statusElement.textContent = 'Opponent\'s turn';
                                    this.isPlayerTurn = false;
                                }
                            } else if (moveData.type === 'MOVE_MADE') {
                                console.log('[Sound Debug] Move made, processing move data:', moveData);
                                console.log('[Sound Debug] Complete move data received:', JSON.stringify(moveData, null, 2));
                                await this.fetchGame();
                                
                                // Timer data is already updated from fetchGame() call above
                                // Just restart timer with the updated data
                                if (this.timeControlMinutes && (this.gameStatus === 'IN_PROGRESS' || this.gameStatus === 'CHECK')) {
                                    this.startTimer();
                                }
                                
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
                                    console.log('[Sound Debug] Move type detected:', moveData.moveType);
                                    const soundType = moveTypeToSound[moveData.moveType] || 'move';
                                    console.log('[Sound Debug] Mapped to sound type:', soundType);
                                    this.playSound(soundType);
                                } else {
                                    console.log('[Sound Debug] No move type in move data, defaulting to move sound');
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
                                console.log('Timeout detected, stopping timer and updating game state');
                                this.stopTimer();
                                await this.fetchGame();
                                
                                // Update game status and show end game popup if it's timeout
                                if (this.gameStatus === 'TIME_OUT') {
                                    this.statusElement.textContent = 'Game over by timeout!';
                                    // The updateBoard method will handle showing the end game popup
                                    this.updateBoard();
                                }
                            }
                        } catch (error) {
                            console.error('Error parsing WebSocket message:', error);
                        }
                    });
                    
                    // Subscribe to chat messages
                    this.stompClient.subscribe('/topic/chat/' + this.gameId, (message) => {
                        try {
                            const chatMessage = JSON.parse(message.body);
                            this.displayChatMessage(chatMessage);
                        } catch (error) {
                            console.error('Error parsing chat message:', error);
                        }
                    });
                } else {
                    console.error('Cannot subscribe: stompClient is not connected');
                }
            },
            (error) => {
                console.error('STOMP connection error:', error);
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
        
        this.chatMessages.appendChild(messageDiv);
        this.chatMessages.scrollTop = this.chatMessages.scrollHeight;
    }

    async handleTileClick(event) {
        // Prevent moves if game hasn't started
        if (this.gameStatus === 'WAITING_FOR_OPPONENT') {
            console.log('Game has not started yet');
            return;
        }
        
        // Prevent moves if game is not in progress (ended)
        if (this.gameStatus !== 'IN_PROGRESS' && this.gameStatus !== 'CHECK') {
            console.log('Game is not in progress - moves are disabled');
            return;
        }
        
        // Prevent moves if in viewing mode (not at latest board)
        if (this.isViewingMode) {
            console.log('In viewing mode - moves are disabled');
            return;
        }
        
        if (!this.isPlayerTurn) {
            console.log('Not your turn');
            return;
        }
        const tile = event.target.closest('.tile');
        if (!tile) return;

        let position = parseInt(tile.dataset.position);
        

        console.log('function handleTileClick start');
        console.log('1. Tile clicked at position:', position);

        // Clear previous selection first
        document.querySelector('.selected')?.classList.remove('selected');

        if (this.selectedSourceTile === null) {
            // First click - select piece
            if (this.hasPiece(tile)) {
                const tileData = this.boardDTO.tiles.find(t => t.tileCoordinate === position);
                
                if (!tileData || !tileData.piece) {
                    console.error('2. Tile has no piece at position:' + position + '... returning');
                    return;
                }
                
                const piece = tileData.piece;
                console.log('2. Piece at position:' + position + ' is ' + piece.pieceAlliance + ' and current player is ' + this.boardDTO.currentPlayer.alliance);
                if ((this.playerColor === 'WHITE' && piece.pieceAlliance === 'WHITE') || 
                    (this.playerColor === 'BLACK' && piece.pieceAlliance === 'BLACK')) {
                    console.log('Piece belongs to current player, selecting...');
                    this.selectedSourceTile = position;
                    tile.classList.add('selected');
                    this.showLegalMoves(position); // Show legal moves
                    console.log('3. Legal moves shown for position:' + position + ' and piece:' + piece);
                } else {
                    console.log('2. Piece does not belong to current player, ignoring...');
                    document.querySelector('.selected')?.classList.remove('selected');
                    this.selectedSourceTile = null;
                    this.clearLegalMoves();
                }
            }
        } else {
            // Second click - make move
            if (this.hasPiece(tile)) {
                const tileData = this.boardDTO.tiles.find(t => t.tileCoordinate === position);
                console.log('Second click - make move');
                console.log('1. Tile coordinate selected on second click:' + tileData.tileCoordinate);
                const piece = tileData?.piece;
                
                if (piece && piece.pieceAlliance === this.boardDTO.currentPlayer.alliance) {
                    // If clicking on another piece of the same color, select that piece instead
                    console.log('Selecting different piece of same color so clearing previous selection and selecting new piece');
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
                    console.log('Not a legal move, clearing selection');
                    document.querySelector('.selected')?.classList.remove('selected');
                    this.selectedSourceTile = null;
                    this.clearLegalMoves();
                    return;
                }
            }
            
            console.log('Making move from', this.selectedSourceTile, 'to', position);
            this.clearLegalMoves(); // Clear previous highlights
            
            try {
                // Get the game ID from the URL path
                const pathParts = window.location.pathname.split('/');
                this.gameId = pathParts[pathParts.length - 1];
                console.log('Game ID for move:', this.gameId);
                
                const response = await fetch(`/api/games/${this.gameId}/move`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        gameId: this.gameId,
                        sourceCoordinate: this.selectedSourceTile,
                        targetCoordinate: position
                    })
                });
                
                // Log the response status and headers for debugging
                console.log('Move response status:', response.status);
                console.log('Move response headers:', [...response.headers.entries()]);
                
                // Try to get the response text regardless of status
                const responseText = await response.text();
                console.log('Move response text:', responseText);
                
                if (!response.ok) {
                    console.error('Move failed:', response.status, responseText);
                    // Don't throw an error, just log it and continue
                    // The move might have been processed despite the error
                } else {
                    // Try to parse the response as JSON if it's not empty
                    if (responseText) {
                        try {
                            const moveResult = JSON.parse(responseText);
                            console.log('Move result:', moveResult);
                        } catch (e) {
                            console.error('Error parsing move result:', e);
                        }
                    }
                }
                
                this.highlightLastMove(this.selectedSourceTile, position);
            } catch (error) {
                console.error('Error making move:', error);
                this.statusElement.textContent = 'Error making move, but it might have been processed';
            } finally {
                // Ensure dragover effects are cleaned up after move attempt
                document.querySelectorAll('.tile.dragover').forEach(tile => {
                    tile.classList.remove('dragover');
                });
            }
            
            // Clear selection
            document.querySelector('.selected')?.classList.remove('selected');
            this.selectedSourceTile = null;
        }
    }

    handleMouseDown(event) {
        if(!this.isPlayerTurn) {
            console.log('Not your turn, ignoring...');
            return;
        }
        
        // Prevent dragging if game is not in progress (ended)
        if (this.gameStatus !== 'IN_PROGRESS' && this.gameStatus !== 'CHECK') {
            console.log('Game is not in progress - dragging is disabled');
            return;
        }
        
        // Prevent dragging if in viewing mode (browsing positions)
        if (this.isViewingMode) {
            console.log('In viewing mode - dragging is disabled');
            return;
        }
        
        const piece = event.target.closest('.piece');
        if (!piece) return;

        const tile = piece.parentElement;
        const position = parseInt(tile.dataset.position);
        const tileData = this.boardDTO.tiles.find(t => t.tileCoordinate === position);
        
        if (!tileData || !tileData.piece) return;

        const pieceData = tileData.piece;
        if (pieceData.pieceAlliance !== this.boardDTO.currentPlayer.alliance) return;

        this.isDragging = true;
        this.draggedPiece = piece;
        this.selectedSourceTile = position;

        // Create drag image with optimized performance
        this.dragImage = document.createElement('div');
        this.dragImage.className = 'piece dragging-piece';
        this.dragImage.style.backgroundImage = piece.style.backgroundImage;
        this.dragImage.style.transform = 'translate(-50%, -50%)'; // Pre-set transform for better performance
        document.body.appendChild(this.dragImage);

        // Set initial position with requestAnimationFrame for smoother animation
        requestAnimationFrame(() => {
            this.dragImage.style.left = event.clientX + 'px';
            this.dragImage.style.top = event.clientY + 'px';
        });

        // Hide original piece with opacity transition
        this.draggedPiece.style.opacity = '0.3';

        // Show legal moves with optimized performance
        this.clearLegalMoves();
        tile.classList.add('selected');
        this.showLegalMoves(position);

        event.preventDefault();
    }

    handleMouseMove(event) {
        if(!this.isPlayerTurn) {
            console.log('Not your turn, ignoring...');
            return;
        }
        if (!this.isDragging || !this.dragImage) return;
        
        // Update dragging piece position (centered on cursor)
        this.dragImage.style.left = event.clientX + 'px';
        this.dragImage.style.top = event.clientY + 'px';
        
        // Add hover effect to tile under cursor
        const hoveredTile = document.elementFromPoint(event.clientX, event.clientY)?.closest('.tile');
        document.querySelectorAll('.tile.dragover').forEach(tile => {
            if (tile !== hoveredTile) tile.classList.remove('dragover');
        });
        if (hoveredTile) hoveredTile.classList.add('dragover');
        
        event.preventDefault();
    }

    async handleMouseUp(event) {
        // Prevent moves if game hasn't started
        if (this.gameStatus === 'WAITING_FOR_OPPONENT') {
            console.log('Game has not started yet');
            return;
        }
        
        // Prevent moves if game is not in progress (ended)
        if (this.gameStatus !== 'IN_PROGRESS' && this.gameStatus !== 'CHECK') {
            console.log('Game is not in progress - moves are disabled');
            return;
        }
        
        if (!this.isPlayerTurn) {
            console.log('Not your turn');
            return;
        }
        if (!this.isDragging) return;

        // Remove any remaining dragover effects
        document.querySelectorAll('.tile.dragover').forEach(tile => {
            tile.classList.remove('dragover');
        });

        // If right click, just cancel the drag
        if (event.button === 2) {
            // Cleanup
            if (this.draggedPiece) {
                this.draggedPiece.style.opacity = '1';
            }
            if (this.dragImage) {
                this.dragImage.remove();
            }
            this.isDragging = false;
            this.draggedPiece = null;
            this.dragImage = null;
            this.selectedSourceTile = null;
            this.clearLegalMoves();
            document.querySelector('.selected')?.classList.remove('selected');
            return;
        }

        const targetTile = event.target.closest('.tile');
        if (targetTile) {
            const targetPosition = parseInt(targetTile.dataset.position);
            if (this.selectedSourceTile !== targetPosition) {
                try {
                    console.log('Making move from', this.selectedSourceTile, 'to', targetPosition);
                    
                    // Get the game ID from the URL path
                    const pathParts = window.location.pathname.split('/');
                    this.gameId = pathParts[pathParts.length - 1];
                    console.log('Game ID for move:', this.gameId);
                    
                    const response = await fetch(`/api/games/${this.gameId}/move`, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify({
                            gameId: this.gameId,
                            sourceCoordinate: this.selectedSourceTile,
                            targetCoordinate: targetPosition
                        })
                    });
                    
                    if (!response.ok) {
                        const errorText = await response.text();
                        console.error('Move failed:', response.status, errorText);
                        throw new Error('Invalid move');
                    }
                    
                    // Get the response data
                    const moveResult = await response.json();
                    console.log('Move result received:', moveResult);
                    
                    // Update the board immediately with the move result
                    if (moveResult.board) {
                        // Check if board is a string (JSON) that needs to be parsed
                        if (typeof moveResult.board === 'string') {
                            try {
                                this.boardDTO = JSON.parse(moveResult.board);
                            } catch (e) {
                                console.error('Error parsing board JSON:', e);
                                this.boardDTO = moveResult.board;
                            }
                        } else {
                            this.boardDTO = moveResult.board;
                        }
                        
                        // Update the board visually
                        this.updateBoard();
                    }
                    
                    // Show visual indicators for the move
                    this.highlightLastMove(this.selectedSourceTile, targetPosition);
                    
                    // Clear selection
                    this.selectedSourceTile = null;
                    this.clearLegalMoves();
                    document.querySelector('.selected')?.classList.remove('selected');
                } catch (error) {
                    console.error('Error making move:', error);
                    this.statusElement.textContent = 'Error making move: ' + error.message;
                }
            }
        }
        
        // Cleanup
        if (this.draggedPiece) {
            this.draggedPiece.style.opacity = '1';
        }
        if (this.dragImage) {
            this.dragImage.remove();
        }
        this.isDragging = false;
        this.draggedPiece = null;
        this.dragImage = null;
    }

    showLegalMoves(position) {
        // Log the position being checked and current board state
        
        // Check if boardDTO and currentPlayer exist
        if (!this.boardDTO || !this.boardDTO.currentPlayer) {
            console.error('Board data or current player is missing');
            return;
        }
        
        // Get the moves array from currentPlayer
        const moves = this.boardDTO.currentPlayer.moves;
        
        // Validate moves array
        if (!moves || !Array.isArray(moves)) {
            console.error('Moves array is missing or invalid');
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
                console.error('Error parsing last move data for en passant:', e);
            }
        }
    }

    clearLegalMoves() {
        // Remove all legal move highlights - using the correct CSS class names
        document.querySelectorAll('.legal-move-non-capture, .legal-move-capture, .legal-move-en-passant').forEach(tile => {
            tile.classList.remove('legal-move-non-capture', 'legal-move-capture', 'legal-move-en-passant');
        });
        
        // Also clear king check highlighting when clearing legal moves
        this.clearKingCheckHighlight();
    }

    hasPiece(tile) {
        return tile.querySelector('.piece') !== null;
    }

    updateBoard() {        
        console.log('Updating board with:', this.boardDTO);
        
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
            
            // Update timer display to show final times
            this.updateTimerDisplay();
            
            // Add visual indicator that timer has stopped
            const timerElements = document.querySelectorAll('.player-timer');
            timerElements.forEach(timer => {
                timer.classList.add('timer-stopped');
            });
            
            console.log('Game end condition detected:', this.gameStatus);
            console.log('Board DTO:', this.boardDTO);
            console.log('White player username:', this.boardDTO.whitePlayerUsername);
            console.log('Black player username:', this.boardDTO.blackPlayerUsername);
            
            this.statusElement.classList.remove('your-turn');
            // Always display white on left, black on right
            const whitePlayerAvatar = this.boardDTO.whitePlayerAvatar || '/images/default-avatar.png';
            const blackPlayerAvatar = this.boardDTO.blackPlayerAvatar || '/images/default-avatar.png';
            const whitePlayerUsername = this.boardDTO.whitePlayerUsername;
            const blackPlayerUsername = this.boardDTO.blackPlayerUsername;
            let winner, result;

            if (this.gameStatus === 'TIME_OUT') {
                // Use the winner information from the game data
                if (this.winnerUsername) {
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
                // For checkmate, determine winner based on current player
                if (this.boardDTO.currentPlayer.alliance === 'WHITE') {
                    // White's turn but checkmate, so black won
                    winner = this.boardDTO.blackPlayerUsername;
                } else {
                    // Black's turn but checkmate, so white won
                    winner = this.boardDTO.whitePlayerUsername;
                }
            } else if (this.gameStatus === 'DRAW') {
                winner = 'Draw';
            } else if (this.gameStatus === 'RESIGNED') {
                // For resignation, the current player resigned, so opponent wins
                if (this.boardDTO.currentPlayer.alliance === 'WHITE') {
                    // White resigned, so black won
                    winner = this.boardDTO.blackPlayerUsername;
                } else {
                    // Black resigned, so white won
                    winner = this.boardDTO.whitePlayerUsername;
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

            this.showGameEndPopup(
                winner,
                result,
                currentPlayerUsername,  // Always current player on left
                currentPlayerColor,     // Current player's color
                opponentUsername,       // Always opponent on right
                opponentColor,          // Opponent's color
                this.gameStatus === 'TIME_OUT' ? 'by timeout' : 'by checkmate'
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
                console.error('Board data does not have tiles array. Board DTO:', JSON.stringify(this.boardDTO));
                return;
            }
            
            let tileCoordinate = index;
            
            const tileData = this.boardDTO.tiles.find(t => t.tileCoordinate === tileCoordinate);
            if (tileData && tileData.tileOccupied) {
                console.log('Tile ' + index + ' is occupied by ' + tileData.piece.pieceAlliance + ' ' + tileData.piece.pieceSymbol);
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
                    tile.appendChild(pieceElement);
                }
            }
        });

        // Highlight king in check with orange color
        console.log('Checking game status for king highlighting:', this.gameStatus);
        if (this.gameStatus === 'CHECK') {
            console.log('Game status is CHECK, highlighting king');
            this.highlightKingInCheck();
        } else {
            console.log('Game status is not CHECK, clearing king highlighting');
            // Clear king check highlighting if not in check
            this.clearKingCheckHighlight();
        }

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
                console.error('Error parsing lastMoveData:', e);
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
            this.highlightKingInCheck();
        }
    }

    /**
     * Highlights the king's square with orange color when the king is in check
     */
    highlightKingInCheck() {
        console.log('highlightKingInCheck called');
        console.log('Game status:', this.gameStatus);
        console.log('Board DTO:', this.boardDTO);
        
        // Clear any existing king check highlighting first
        this.clearKingCheckHighlight();
        
        // Find the king of the current player (the one in check)
        const currentPlayerAlliance = this.boardDTO.currentPlayer.alliance;
        console.log('Current player alliance:', currentPlayerAlliance);
        
        const kingTile = this.boardDTO.tiles.find(tile => 
            tile.tileOccupied && 
            tile.piece.pieceSymbol === 'KING' && 
            tile.piece.pieceAlliance === currentPlayerAlliance
        );
        
        console.log('Found king tile:', kingTile);
        
        if (kingTile) {
            // Find the corresponding DOM tile and add the check highlighting class
            const tileElement = this.board.querySelector(`.tile[data-position='${kingTile.tileCoordinate}']`);
            console.log('Found tile element:', tileElement);
            
            if (tileElement) {
                tileElement.classList.add('king-in-check');
                console.log(`King in check highlighted at position ${kingTile.tileCoordinate}`);
                console.log('Tile element classes after adding:', tileElement.className);
            } else {
                console.error('Could not find tile element for king at position:', kingTile.tileCoordinate);
            }
        } else {
            console.error('Could not find king tile for alliance:', currentPlayerAlliance);
            console.log('Available tiles:', this.boardDTO.tiles);
        }
    }

    /**
     * Clears the king check highlighting
     */
    clearKingCheckHighlight() {
        // Remove king check highlighting from all tiles
        document.querySelectorAll('.tile.king-in-check').forEach(tile => {
            tile.classList.remove('king-in-check');
        });
    }

    showGameEndPopup(winner, result, currentPlayerUsername, currentPlayerColor, opponentUsername, opponentColor, subtitle) {
        // Remove existing popup if any
        let existing = document.getElementById('game-end-popup');
        if (existing) existing.remove();

        // Create popup
        const popup = document.createElement('div');
        popup.id = 'game-end-popup';
        popup.innerHTML = `
            <button class="popup-close" id="close-game-end-popup" title="Close">&#10005;</button>
            <div class="popup-title">Game Over</div>
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
        `;
        document.body.appendChild(popup);

        // Close button
        document.getElementById('close-game-end-popup').onclick = () => popup.remove();

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
            @keyframes popupAppear {
                0% { transform: translate(-50%, -50%) scale(0.7); opacity: 0; }
                100% { transform: translate(-50%, -50%) scale(1); opacity: 1; }
            }
            `;
            document.head.appendChild(style);
        }
    }

    // Update the playSound method with better debugging
    playSound(moveType) {
        if (this.sounds[moveType]) {
            this.sounds[moveType].play().catch(e => {
                console.error(`[Sound Debug] Error playing ${moveType} sound:`, e);
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
            console.log('Loading game positions for analysis...');
            
            // Fetch all game positions from the server
            const response = await fetch(`/api/games/${this.gameId}/history`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            });
            
            if (!response.ok) {
                console.error('Failed to load game positions:', response.status);
            return;
        }
        
            // Parse the response to get all positions
            this.gamePositions = await response.json();
            console.log(`Loaded ${this.gamePositions.length} game positions`);
            
            // Store the latest position for comparison
            if (this.gamePositions.length > 0) {
                this.latestPositionBoardDTO = this.gamePositions[this.gamePositions.length - 1].board;
            }
            
            // Update navigation button states (this will also set viewing mode)
            this.updateNavigationButtons();
            
            // Update viewing mode status
            this.updateViewingModeStatus(null);
            
        } catch (error) {
            console.error('Error loading game positions:', error);
        }
    }
    
    /**
     * Navigate to the previous position in the game history
     * This method moves backward through the move history
     */
    async navigateToPreviousPosition() {
        console.log(`Navigating to previous position. Current index: ${this.currentPositionIndex}, Total positions: ${this.gamePositions.length}`);
        
        // If we're at the latest position (index -1), go to the last position in history
        if (this.currentPositionIndex === -1) {
            if (this.gamePositions.length === 0) {
                console.log('No positions available');
                return;
            }
            this.currentPositionIndex = this.gamePositions.length - 1;
            console.log(`Going from latest position to last position in history: ${this.currentPositionIndex}`);
        } else if (this.currentPositionIndex <= 0) {
            // If we're at the initial position, we can't go back further
            console.log('Already at the initial position');
            return;
        } else {
            // Move to the previous position
            this.currentPositionIndex--;
            console.log(`Moving to previous position: ${this.currentPositionIndex}`);
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
            console.log('No game positions available');
            return;
        }
        
        // Move to the initial position (index 0)
        this.currentPositionIndex = 0;
        console.log('Navigating to initial position');
        
        // Load and display the position
        await this.displayPosition(this.currentPositionIndex);
        
        // Update navigation button states
        this.updateNavigationButtons();
    }
    
    /**
     * Navigate to the next position in the game history
     * This method moves forward through the move history
     */
    async navigateToNextPosition() {
        console.log(`Navigating to next position. Current index: ${this.currentPositionIndex}, Total positions: ${this.gamePositions.length}`);
        
        // If we're at the latest position, we can't go forward further
        if (this.currentPositionIndex === -1) {
            console.log('Already at the latest position');
            return;
        }
        
        // If we're at the last position in history, go to the latest position
        if (this.currentPositionIndex >= this.gamePositions.length - 1) {
            console.log('Going from last position in history to latest position');
            await this.returnToLatestPosition();
            return;
        }
        
        // Move to the next position
        this.currentPositionIndex++;
        console.log(`Moving to next position: ${this.currentPositionIndex}`);
        
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
        console.log(`displayPosition called with index: ${positionIndex}`);
        console.log(`Game positions array length: ${this.gamePositions.length}`);
        console.log(`Game positions:`, this.gamePositions);
        
        // Validate position index
        if (positionIndex < 0 || positionIndex >= this.gamePositions.length) {
            console.error('Invalid position index:', positionIndex);
            return;
        }
        
        // Get the position data
        const position = this.gamePositions[positionIndex];
        console.log(`Displaying position ${positionIndex}:`, position);
        console.log(`Position board data:`, position.board);
        
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
        console.log('Returning to latest position');
        
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
     */
    updateNavigationButtons() {
        if (!this.prevPositionBtn || !this.nextPositionBtn) {
            console.error('Navigation buttons not found');
            return;
        }
        
        console.log(`Updating navigation buttons - Current index: ${this.currentPositionIndex}, Total positions: ${this.gamePositions.length}`);
        
        // If no positions available, disable both buttons
        if (this.gamePositions.length === 0) {
            this.prevPositionBtn.disabled = true;
            this.nextPositionBtn.disabled = true;
            console.log('No positions available - both buttons disabled');
            return;
        }
        
        // Previous button: always clickable until we reach the first board (index 0)
        // When at latest position (index -1), we can go back to the last position in history
        const canGoBack = this.currentPositionIndex > 0 || this.currentPositionIndex === -1;
        this.prevPositionBtn.disabled = !canGoBack;
        
        // Next button: clickable when we are in viewing mode (not at latest board)
        // When at latest position (index -1), we can't go forward
        const isAtLatestBoard = this.currentPositionIndex === -1;
        const canGoForward = !isAtLatestBoard;
        this.nextPositionBtn.disabled = !canGoForward;
        
        // Update viewing mode based on whether we're at the latest board
        this.isViewingMode = !isAtLatestBoard;
        
        console.log(`Navigation buttons - Can go back: ${canGoBack}, Can go forward: ${canGoForward}, Is viewing: ${this.isViewingMode}`);
        console.log(`Button states - Previous disabled: ${this.prevPositionBtn.disabled}, Next disabled: ${this.nextPositionBtn.disabled}`);
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
        console.log('Setting up position navigation listeners...');
        console.log('Previous button element:', this.prevPositionBtn);
        console.log('Next button element:', this.nextPositionBtn);
        
        // Force enable buttons temporarily for testing
        if (this.prevPositionBtn) {
            this.prevPositionBtn.disabled = false;
            console.log('Forced previous button enabled for testing');
        }
        if (this.nextPositionBtn) {
            this.nextPositionBtn.disabled = false;
            console.log('Forced next button enabled for testing');
        }
        
        // Bind click handlers to navigation buttons
        if (this.prevPositionBtn) {
            console.log('Adding click listener to previous button');
            this.prevPositionBtn.addEventListener('click', (e) => {
                console.log('Previous button clicked!');
                e.preventDefault();
                this.navigateToPreviousPosition();
            });
        } else {
            console.error('Previous button not found!');
        }
        
        if (this.nextPositionBtn) {
            console.log('Adding click listener to next button');
            this.nextPositionBtn.addEventListener('click', (e) => {
                console.log('Next button clicked!');
                e.preventDefault();
                this.navigateToNextPosition();
            });
        } else {
            console.error('Next button not found!');
        }
        
        // Add keyboard navigation support
        document.addEventListener('keydown', (e) => {
            // Only handle navigation keys if we're in viewing mode
            if (!this.isViewingMode) return;
            
            switch (e.key) {
                case 'ArrowLeft':
                    e.preventDefault();
                    if (!this.prevPositionBtn.disabled) {
                        this.navigateToPreviousPosition();
                    }
                    break;
                case 'ArrowRight':
                    e.preventDefault();
                    if (!this.nextPositionBtn.disabled) {
                        this.navigateToNextPosition();
                    }
                    break;
                case 'Escape':
                    e.preventDefault();
                    this.returnToLatestPosition();
                    break;
            }
        });
    }
}

// Initialize the game when the page loads
document.addEventListener('DOMContentLoaded', () => {
    console.log('DOM content loaded, initializing game...');
    const gameBoard = document.getElementById('game-board');
    if (gameBoard) {
        // Initialize the chess game
        window.chessGame = new ChessGame();
        
        // Get game ID from URL path
        const pathParts = window.location.pathname.split('/');
        const gameId = pathParts[pathParts.length - 1];
        console.log('Game initialized with ID from URL:', gameId);
        
        if (gameId) {
            // Set the game ID in the chess game instance
            window.chessGame.gameId = gameId;
            // Start the game automatically
            if (window.chessGame) {
                console.log('Fetching game automatically...');
                window.chessGame.fetchGame();
            }
        } else {
            console.error('Could not find game ID in URL path');
        }
    } else {
        console.error('Could not find game board element');
    }
}); 