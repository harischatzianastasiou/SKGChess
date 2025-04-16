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
            'WHITE_PAWN': '/images/white_p.png',
            'WHITE_KNIGHT': '/images/white_n.png',
            'WHITE_BISHOP': '/images/white_b.png',
            'WHITE_ROOK': '/images/white_r.png',
            'WHITE_QUEEN': '/images/white_q.png',
            'WHITE_KING': '/images/white_k.png',
            'BLACK_PAWN': '/images/black_p.png',
            'BLACK_KNIGHT': '/images/black_n.png',
            'BLACK_BISHOP': '/images/black_b.png',
            'BLACK_ROOK': '/images/black_r.png',
            'BLACK_QUEEN': '/images/black_q.png',
            'BLACK_KING': '/images/black_k.png'
        };

        this.initializeBoard();
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
    }

    initializeBoard() {
        console.log('Setting up board...');
        this.board.innerHTML = '';
        const files = ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'];
        const ranks = ['8', '7', '6', '5', '4', '3', '2', '1'];
        
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
                
                // Add file letter (a-h)
                const fileLabel = document.createElement('div');
                fileLabel.className = 'coordinate-file';
                fileLabel.textContent = files[col];
                tile.appendChild(fileLabel);
                
                // Add rank number (1-8)
                const rankLabel = document.createElement('div');
                rankLabel.className = 'coordinate-rank';
                rankLabel.textContent = ranks[row];
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
        
        // Debug the board data structure
        console.log('Board DTO structure:', this.boardDTO);
        console.log('Has tiles property:', this.boardDTO.hasOwnProperty('tiles'));
        console.log('Tiles is array:', Array.isArray(this.boardDTO.tiles));
        
        this.gameStatus = gameData.status;
        this.gameId = gameData.id;
        this.lastMoveData = gameData.lastMoveData; // Store lastMoveData in the class
        if (gameData.whitePlayerId === this.userId) {
            this.boardOrientation = 'WHITE';
            this.playerColor = 'WHITE';
        } else if (gameData.blackPlayerId === this.userId) {
            this.boardOrientation = 'BLACK';
            this.playerColor = 'BLACK';
        }
        if(this.boardDTO.currentPlayer.alliance === this.playerColor) {
            this.isPlayerTurn = true;
        } else {
            this.isPlayerTurn = false;
        }
        this.updateBoard();
    } catch (error) {
        console.error('Error fetching game:', error);
        this.statusElement.textContent = 'Error fetching game';
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
                            // Parse the message body as JSON
                            const moveData = JSON.parse(message.body);
                            console.log('Parsed WebSocket message:', moveData);
                            
                            // Handle different message types
                            if (moveData.type === 'GAME_STARTED') {
                                this.statusElement.textContent = 'Game in progress - Your turn';
                            } else if (moveData.type === 'MOVE_MADE') {
                                console.log('Move made, fetching updated game state');
                                await this.fetchGame();
                                // No need to call updateBoard() here as it's called inside fetchGame()
                                
                                // Update status message based on game status
                                if (this.gameStatus === 'CHECKMATE') {
                                    this.statusElement.textContent = 'Checkmate';
                                } else if (this.gameStatus === 'DRAW') {
                                    this.statusElement.textContent = 'Draw';
                                } else if (this.gameStatus === 'IN_PROGRESS') {
                                    // Check if it's the current player's turn
                                    const isCurrentPlayerTurn = 
                                        (this.playerColor === 'WHITE' && this.boardDTO.currentPlayer.alliance === 'WHITE') ||
                                        (this.playerColor === 'BLACK' && this.boardDTO.currentPlayer.alliance === 'BLACK');
                                    
                                    this.statusElement.textContent = isCurrentPlayerTurn ? 'Your turn' : 'Opponent\'s turn';
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
        if(!this.isPlayerTurn) {
            console.log('Not your turn, ignoring...');
            return;
        }
        const tile = event.target.closest('.tile');
        if (!tile) return;

        const position = parseInt(tile.dataset.position);
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

        // Create drag image
        this.dragImage = document.createElement('div');
        this.dragImage.className = 'piece dragging-piece';
        this.dragImage.style.backgroundImage = piece.style.backgroundImage;
        this.dragImage.style.width = '60px';
        this.dragImage.style.height = '60px';
        this.dragImage.style.position = 'fixed';
        this.dragImage.style.pointerEvents = 'none';
        this.dragImage.style.zIndex = '1000';
        document.body.appendChild(this.dragImage);

        // Set initial position
        this.dragImage.style.left = (event.clientX - 30) + 'px';
        this.dragImage.style.top = (event.clientY - 30) + 'px';

        // Hide original piece
        this.draggedPiece.style.opacity = '0.3';

        // Show legal moves
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
        
        this.dragImage.style.left = (event.clientX - 30) + 'px';
        this.dragImage.style.top = (event.clientY - 30) + 'px';
        
        // Add hover effect to tile under cursor
        const hoveredTile = document.elementFromPoint(event.clientX, event.clientY)?.closest('.tile');
        document.querySelectorAll('.tile.dragover').forEach(tile => {
            if (tile !== hoveredTile) tile.classList.remove('dragover');
        });
        if (hoveredTile) hoveredTile.classList.add('dragover');
        
        event.preventDefault();
    }

    async handleMouseUp(event) {
        if(!this.isPlayerTurn) {
            console.log('Not your turn, ignoring...');
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
        if (this.boardDTO.checkmate) {
            this.statusElement.textContent = 'Checkmate!';
            this.statusElement.classList.remove('your-turn');
        } else if (this.boardDTO.stalemate) {
            this.statusElement.textContent = 'Stalemate - Game Draw';
            this.statusElement.classList.remove('your-turn');
        } else if (this.boardDTO.draw) {
            this.statusElement.textContent = 'Game Draw';
            this.statusElement.classList.remove('your-turn');
        } else if (this.boardDTO.check) {
            if (this.isPlayerTurn) {
                this.statusElement.textContent = 'Your turn - You are in check!';
                this.statusElement.classList.add('your-turn');
            } else {
                this.statusElement.textContent = 'Opponent in check';
                this.statusElement.classList.remove('your-turn');
            }
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

        // Clear previous highlights
        document.querySelectorAll('.last-move-highlight').forEach(tile => {
            tile.classList.remove('last-move-highlight');
        });

        // Create SVG container
        const svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
        svg.setAttribute("class", "last-move-arrow");
        svg.style.width = this.board.offsetWidth + "px";
        svg.style.height = this.board.offsetHeight + "px";

        // Calculate tile centers
        const tileSize = 70; // Your tile size
        const sourceRow = Math.floor(sourcePos / 8);
        const sourceCol = sourcePos % 8;
        const targetRow = Math.floor(targetPos / 8);
        const targetCol = targetPos % 8;

        const startX = (sourceCol * tileSize) + (tileSize / 2);
        const startY = (sourceRow * tileSize) + (tileSize / 2);
        const endX = (targetCol * tileSize) + (tileSize / 2);
        const endY = (targetRow * tileSize) + (tileSize / 2);

        // Create path
        const path = document.createElementNS("http://www.w3.org/2000/svg", "path");
        path.setAttribute("d", `M ${startX} ${startY} L ${endX} ${endY}`);

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