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
        this.isPlayerTurn = null;
        this.lastMoveArrow = null;
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
        this.initializeArrowMarker();
        
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
        
        // Initialize the game asynchronously
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

        // Remove the redundant animation trigger for black player
        // The animation will now only show when receiving the GAME_STARTED websocket message
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

        // Set player color and board orientation
        if (gameData.whitePlayerId === this.userId) {
            this.playerColor = 'WHITE';
            this.board.classList.remove('black-perspective');
        } else if (gameData.blackPlayerId === this.userId) {
            this.playerColor = 'BLACK';
            this.board.classList.add('black-perspective');
        }

        // Reinitialize the board with correct coordinate labels
        this.initializeBoard();

        if(this.boardDTO.currentPlayer.alliance === this.playerColor) {
            this.isPlayerTurn = true;
        } else {
            this.isPlayerTurn = false;
        }
        this.updateBoard();
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
        
        const socket = new SockJS('/chess-websocket'); // Create a new SockJS connection
        this.stompClient = Stomp.over(socket); // Wrap the socket with Stomp
        
        // Enable debug logging for STOMP
        this.stompClient.debug = function(str) {
            console.log('STOMP: ' + str);
        };
    
        this.stompClient.connect({}, 
            (frame) => {
                console.log('Connected to WebSocket: ' + frame);
                this.reconnectAttempts = 0; // Reset reconnection attempts on successful connection
                
                // Subscribe to the queue messages for this session
                console.log('Subscribing to /topic/game/' + this.gameId);
                
                // Check if stompClient is valid before subscribing
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
                                
                                // Update the status based on player color
                                if (this.playerColor === 'WHITE') {
                                    this.statusElement.textContent = 'Your turn';
                                    this.isPlayerTurn = true;
                                } else {
                                    this.statusElement.textContent = 'Opponent\'s turn';
                                    this.isPlayerTurn = false;
                                }
                            } else if (moveData.type === 'MOVE_MADE') {
                                console.log('Move made, fetching updated game state');
                                await this.fetchGame();
                                // No need to call updateBoard() here as it's called inside fetchGame()
                                
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
                            }
                        } catch (error) {
                            console.error('Error parsing WebSocket message:', error);
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
    
        // socket.onclose = function() {
        //     console.log('WebSocket connection closed'); // Log when the connection is closed
        //     handleDisconnect(); // Handle disconnection
        // };
    }

    async handleTileClick(event) {
        // Prevent moves if game hasn't started
        if (this.gameStatus === 'WAITING_FOR_OPPONENT') {
            console.log('Game has not started yet');
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

        // --- Frontend-only en passant highlight ---
        if (this.lastMoveData) {
            try {
                const lastMoveDataObj = JSON.parse(this.lastMoveData);
                const lastMoveSource = lastMoveDataObj.sourceCoordinate;
                const lastMoveTarget = lastMoveDataObj.targetCoordinate;
                const lastMoveType = lastMoveDataObj.moveType;
                if (lastMoveType === 'PAWN_JUMP') {
                    // For each tile, check if a pawn can capture en passant
                    this.boardDTO.tiles.forEach(sourceTileData => {
                        if (sourceTileData.piece?.pieceSymbol === 'PAWN') {
                            const pawn = sourceTileData.piece;
                            const pawnCoord = sourceTileData.tileCoordinate;
                            const direction = pawn.pieceAlliance === 'WHITE' ? -8 : 8;
                            // The jumped pawn must be adjacent
                            if (Math.abs(pawnCoord - lastMoveTarget) === 1) {
                                // The en passant target is behind the jumped pawn
                                const enPassantTarget = lastMoveTarget + direction;
                                // Only highlight if the target is empty
                                const enPassantTile = this.boardDTO.tiles.find(t => t.tileCoordinate === enPassantTarget && !t.tileOccupied);
                                if (enPassantTile) {
                                    const targetTile = this.board.querySelector(`.tile[data-position='${enPassantTarget}']`);
                                    if (targetTile) {
                                        // Add a special highlight for en passant
                                        targetTile.classList.add('legal-move-en-passant');
                                    }
                                }
                            }
                        }
                    });
                }
            } catch (e) {
                console.error('Error parsing last move data for en passant:', e);
            }
        }
    }

    clearLegalMoves() {
        // Remove all legal move highlights - using the correct CSS class names
        document.querySelectorAll('.legal-move-non-capture, .legal-move-capture').forEach(tile => {
            tile.classList.remove('legal-move-non-capture', 'legal-move-capture');
        });
    }

    hasPiece(tile) {
        return tile.querySelector('.piece') !== null;
    }

    updateBoard() {        
        console.log('Updating board with:', this.boardDTO);
        
        // If game hasn't started, show waiting message
        if (this.gameStatus === 'WAITING_FOR_OPPONENT') {
            this.statusElement.textContent = 'Waiting for opponent to join...';
            this.statusElement.classList.remove('your-turn');
            this.isPlayerTurn = false;
            return;
        }
        
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

        // Check for game end conditions
        if (this.gameStatus === 'CHECKMATE' || this.gameStatus === 'DRAW' || this.gameStatus === 'RESIGNED'
            || this.gameStatus === 'STALEMATE' || this.gameStatus === 'THREEFOLD_REPETITION'
            || this.gameStatus === 'FIFTY_MOVE_RULE' || this.gameStatus === 'INSUFFICIENT_MATERIAL'
            || this.gameStatus === 'MUTUAL_AGREEMENT'
        ) {
            console.log('Game end condition detected:', this.gameStatus);
            console.log('Board DTO:', this.boardDTO);
            console.log('White player username:', this.boardDTO.whitePlayerUsername);
            console.log('Black player username:', this.boardDTO.blackPlayerUsername);
            
            this.statusElement.classList.remove('your-turn');
            // Always display white on left, black on right
            const whitePlayerAvatar = this.boardDTO.whitePlayerAvatar || '/images/white-k.png';
            const blackPlayerAvatar = this.boardDTO.blackPlayerAvatar || '/images/black-k.png';
            const whitePlayerUsername = this.boardDTO.whitePlayerUsername;
            const blackPlayerUsername = this.boardDTO.blackPlayerUsername;
            let winner, result;

            if(this.gameStatus === 'CHECKMATE') {
                this.statusElement.textContent = 'Checkmate!';
                if (this.boardDTO.currentPlayer.alliance === 'WHITE') {
                    winner = this.boardDTO.blackPlayerUsername;
                    result = '0-1';
                } else {
                    winner = this.boardDTO.whitePlayerUsername;
                    result = '1-0';
                }
            } else if(this.gameStatus === 'DRAW') {
                this.statusElement.textContent = 'Draw!';
                winner = 'Draw';
                result = 'Draw';
            } else if(this.gameStatus === 'RESIGNED') {
                this.statusElement.textContent = 'Resigned!';
                winner = this.boardDTO.currentPlayer.alliance === 'WHITE' ? blackPlayerUsername : whitePlayerUsername;
                result = this.boardDTO.currentPlayer.alliance === 'WHITE' ? '0-1' : '1-0';
            } else if(this.gameStatus === 'STALEMATE') {
                this.statusElement.textContent = 'Draw by stalemate!';
                winner = 'Draw';
                result = 'Draw';
            } else if(this.gameStatus === 'THREEFOLD_REPETITION') {
                this.statusElement.textContent = 'Draw by threefold repetition!';
                winner = 'Draw';
                result = 'Draw';
            } else if(this.gameStatus === 'FIFTY_MOVE_RULE') {
                this.statusElement.textContent = 'Draw by fifty move rule!';
                winner = 'Draw';
                result = 'Draw';
            } else if(this.gameStatus === 'INSUFFICIENT_MATERIAL') {
                this.statusElement.textContent = 'Draw by insufficient material!';
                winner = 'Draw';
                result = 'Draw';
            } else if(this.gameStatus === 'MUTUAL_AGREEMENT') {
                this.statusElement.textContent = 'Draw by mutual agreement!';
                winner = 'Draw';
                result = 'Draw';
            }

 
            this.showGameEndPopup(
                winner,
                result,
                winner === 'Draw' ? 'Draw' : (winner === whitePlayerUsername ? whitePlayerUsername : blackPlayerUsername),
                winner === 'Draw' ? '/images/draw.png' : (winner === whitePlayerUsername ? whitePlayerAvatar : blackPlayerAvatar),
                winner === 'Draw' ? 'Draw' : (winner === whitePlayerUsername ? blackPlayerUsername : whitePlayerUsername),
                winner === 'Draw' ? '/images/draw.png' : (winner === whitePlayerUsername ? blackPlayerAvatar : whitePlayerAvatar)
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

        // Check if lastMoveData exists before trying to use it
        if (this.lastMoveData) {
            const lastMoveDataArray = JSON.parse(this.lastMoveData);
            const lastMoveSourceCoordinate = lastMoveDataArray[0];
            const lastMoveTargetCoordinate = lastMoveDataArray[1];
            document.querySelector('.selected')?.classList.remove('selected');
            this.clearLegalMoves();
            document.querySelectorAll('.tile.dragover').forEach(tile => {
                tile.classList.remove('dragover');
            });
            // Clear selection
            document.querySelector('.selected')?.classList.remove('selected');
            this.selectedSourceTile = null;
            this.highlightLastMove(lastMoveSourceCoordinate, lastMoveTargetCoordinate);
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

    initializeArrowMarker() {
        // Create SVG definitions for the arrow marker
        const svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
        svg.style.position = 'absolute';
        svg.style.width = '0';
        svg.style.height = '0';
        
        const defs = document.createElementNS("http://www.w3.org/2000/svg", "defs");
        const marker = document.createElementNS("http://www.w3.org/2000/svg", "marker");
        marker.setAttribute("id", "arrowhead");
        marker.setAttribute("markerWidth", "10");
        marker.setAttribute("markerHeight", "7");
        marker.setAttribute("refX", "9");
        marker.setAttribute("refY", "3.5");
        marker.setAttribute("orient", "auto");
        
        const polygon = document.createElementNS("http://www.w3.org/2000/svg", "polygon");
        polygon.setAttribute("points", "0 0, 10 3.5, 0 7");
        polygon.setAttribute("fill", "rgba(255, 255, 255, 0.7)");
        
        marker.appendChild(polygon);
        defs.appendChild(marker);
        svg.appendChild(defs);
        document.body.appendChild(svg);
    }

    showLastMoveArrow(sourcePos, targetPos) {
        // Remove existing arrow if any
        if (this.lastMoveArrow) {
            this.lastMoveArrow.remove();
        }

        // Clear previous highlights efficiently
        const highlights = document.querySelectorAll('.last-move-highlight');
        highlights.forEach(tile => tile.classList.remove('last-move-highlight'));

        // Create SVG container with optimized attributes
        const svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
        svg.setAttribute("class", "last-move-arrow");
        svg.style.width = this.board.offsetWidth + "px";
        svg.style.height = this.board.offsetHeight + "px";

        // Calculate tile centers with optimized math
        const tileSize = 70;
        let sourceRow = Math.floor(sourcePos / 8);
        let sourceCol = sourcePos % 8;
        let targetRow = Math.floor(targetPos / 8);
        let targetCol = targetPos % 8;

        // Transform coordinates if player is black - optimized calculation
        if (this.playerColor === 'BLACK') {
            sourceRow = 7 - sourceRow;
            sourceCol = 7 - sourceCol;
            targetRow = 7 - targetRow;
            targetCol = 7 - targetCol;
        }

        // Calculate positions with single operation
        const startX = (sourceCol * tileSize) + (tileSize / 2);
        const startY = (sourceRow * tileSize) + (tileSize / 2);
        const endX = (targetCol * tileSize) + (tileSize / 2);
        const endY = (targetRow * tileSize) + (tileSize / 2);

        // Create path with optimized attributes
        const path = document.createElementNS("http://www.w3.org/2000/svg", "path");
        path.setAttribute("d", `M ${startX} ${startY} L ${endX} ${endY}`);

        // Append elements efficiently
        svg.appendChild(path);
        this.board.appendChild(svg);
        this.lastMoveArrow = svg;
    }

    highlightLastMove(sourcePos, targetPos) {
        // Clear previous highlights
        document.querySelectorAll('.last-move-source, .last-move-target').forEach(tile => {
            tile.classList.remove('last-move-source', 'last-move-target');
        });

        // Add highlights to source and target tiles
        const sourceTile = this.board.querySelector(`.tile[data-position='${sourcePos}']`);
        const targetTile = this.board.querySelector(`.tile[data-position='${targetPos}']`);
        
        if (sourceTile) sourceTile.classList.add('last-move-source');
        if (targetTile) targetTile.classList.add('last-move-target');
    }

    showGameStartAnimation() {
        const overlay = document.getElementById('game-start-overlay');
        overlay.classList.add('show');
        
        // Remove the show class after animation completes
        setTimeout(() => {
            overlay.classList.remove('show');
        }, 2000);
    }

    showGameEndPopup(winner, result, winnerUsername, winnerAvatar, loserUsername, loserAvatar) {
        // Remove existing popup if any
        let existing = document.getElementById('game-end-popup');
        if (existing) existing.remove();

        // Create popup
        const popup = document.createElement('div');
        popup.id = 'game-end-popup';
        popup.innerHTML = `
            <button class="popup-close" id="close-game-end-popup" title="Close">&#10005;</button>
            <div class="popup-title">Game Over</div>
            <div class="popup-subtitle">by checkmate</div>
            <div class="popup-players">
                <div class="popup-player ${result === '1-0' ? 'popup-winner' : ''}">
                    <img src="${winnerAvatar}" class="popup-avatar" alt="Winner">
                    <div class="popup-username">${winnerUsername}</div>
                </div>
                <div class="popup-result-center">${result}</div>
                <div class="popup-player ${result === '0-1' ? 'popup-winner' : ''}">
                    <img src="${loserAvatar}" class="popup-avatar" alt="Loser">
                    <div class="popup-username">${loserUsername}</div>
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
#game-end-popup .popup-avatar {
    width: 64px;
    height: 64px;
    border-radius: 50%;
    border: 3px solid #fff;
    margin-bottom: 0.5rem;
    object-fit: cover;
    background: #222;
}
#game-end-popup .popup-winner .popup-avatar {
    border: 3px solid var(--color-accent, #ffd700);
    box-shadow: 0 0 12px 2px var(--color-accent, #ffd700);
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