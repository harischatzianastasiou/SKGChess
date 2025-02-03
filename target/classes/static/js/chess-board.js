class ChessGame {
    constructor(gameData) {
        this.board = document.getElementById('game-board');
        if (!this.board) {
            console.error('Chess board element not found');
            return;
        }
        
        this.piece = document.getElementById('piece');
        this.statusElement = document.getElementById('status');
        this.currentGameState = gameData || null;
        this.selectedSourceTile = null;
        this.selectedTargetTile = null;
        this.isDragging = false;
        this.draggedPiece = null;
        this.dragImage = null;
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

        // Get current player's username
        const usernameElement = document.getElementById('username');
        this.currentPlayerUsername = usernameElement ? usernameElement.textContent.trim() : null;
        console.log('Current player:', this.currentPlayerUsername);

        //WebSocket setup
        this.stompClient = null;
        this.connected = false;
        this.connectWebSocket();

        // Initialize board after WebSocket connection
        if (this.currentGameState) {
            console.log('Initializing with game data:', this.currentGameState);
            this.initializeBoard();
            this.updateGameStatus();
        }

        // Setup event listeners after board is initialized
        this.setupEventListeners();
    }

    setupEventListeners() {
        // Start game button listener
        const startGameButton = document.getElementById('start-game');
        if (startGameButton) {
            startGameButton.addEventListener('click', () => this.startGame());
        }
        
        if (this.board) {
            // Remove any existing listeners first
            const newBoard = this.board.cloneNode(true);
            this.board.parentNode.replaceChild(newBoard, this.board);
            this.board = newBoard;

            // Add click listener
            this.board.addEventListener('click', (e) => this.handleTileClick(e));
            
            // Mouse event listeners for dragging
            this.board.addEventListener('mousedown', (e) => this.handleMouseDown(e));
            document.addEventListener('mousemove', (e) => this.handleMouseMove(e));
            document.addEventListener('mouseup', (e) => this.handleMouseUp(e));

            this.board.addEventListener('contextmenu', (event) => {
                event.preventDefault();
                if (this.selectedSourceTile !== null) {
                    this.clearLegalMoves();
                    document.querySelector('.selected')?.classList.remove('selected');
                    this.selectedSourceTile = null;
                }
            });
        }
    }

    startGame() {
        if (!this.connected) {
            console.error('WebSocket not connected');
            return;
        }

        try {
            // Send game start message through WebSocket
            this.stompClient.send("/app/game/start", {}, JSON.stringify({
                gameId: this.currentGameState.id
            }));
            
            // Update UI
            const startButton = document.getElementById('start-game');
            if (startButton) {
                startButton.remove();
            }
            this.statusElement.textContent = 'Starting game...';
        } catch (error) {
            console.error('Error starting game:', error);
            this.statusElement.textContent = 'Error starting game';
        }
    }

    handleGameUpdate(gameEvent) {
        console.log('Handling game update:', gameEvent);
        
        if (!gameEvent) return;

        switch(gameEvent.type) {
            case 'GAME_CREATED':
                console.log('Game created event received');
                this.currentGameState = gameEvent.game;
                this.initializeBoard();
                this.updateGameStatus();
                break;

            case 'GAME_STARTED':
                console.log('Game started event received');
                this.currentGameState = gameEvent.game;
                this.initializeBoard();
                this.updateGameStatus();
                // Remove start button if it exists
                document.getElementById('start-game')?.remove();
                break;

            case 'MOVE_MADE':
                console.log('Move made event received');
                this.currentGameState = gameEvent.game;
                this.updateBoardFromFen(gameEvent.game.fenPosition);
                this.updateGameStatus();
                if (gameEvent.move) {
                    this.highlightLastMove(gameEvent.move.sourcePosition, gameEvent.move.targetPosition);
                }
                break;

            case 'GAME_ENDED':
                console.log('Game ended event received');
                this.currentGameState = gameEvent.game;
                this.updateGameStatus();
                break;
        }
    }

    updateGameStatus() {
        if (!this.currentGameState || !this.statusElement) return;

        const isWhitePlayer = this.currentPlayerUsername === this.currentGameState.whitePlayer;
        const isBlackPlayer = this.currentPlayerUsername === this.currentGameState.blackPlayer;
        
        // Get current turn from FEN
        const fenParts = this.currentGameState.fenPosition.split(' ');
        const isWhiteTurn = fenParts[1] === 'w';

        // Update player statuses
        const whiteStatus = document.querySelector('.white-player .player-status');
        const blackStatus = document.querySelector('.black-player .player-status');
        
        if (whiteStatus) whiteStatus.textContent = isWhiteTurn ? '(Current Turn)' : '';
        if (blackStatus) blackStatus.textContent = !isWhiteTurn ? '(Current Turn)' : '';

        if (this.currentGameState.status === 'ACTIVE') {
            if ((isWhiteTurn && isWhitePlayer) || (!isWhiteTurn && isBlackPlayer)) {
                this.statusElement.textContent = 'Your turn';
                this.board.classList.add('active-player');
            } else if (isWhitePlayer || isBlackPlayer) {
                this.statusElement.textContent = 'Opponent\'s turn';
                this.board.classList.remove('active-player');
            } else {
                this.statusElement.textContent = isWhiteTurn ? 'White\'s turn' : 'Black\'s turn';
            }
        } else {
            this.statusElement.textContent = this.currentGameState.status;
            this.board.classList.remove('active-player');
        }
    }

    updateBoardFromFen(fen) {
        if (!fen) return;
        
        const fenBoard = fen.split(' ')[0];
        const rows = fenBoard.split('/');
        
        this.board.innerHTML = '';
        let position = 0;

        rows.forEach((row, rowIndex) => {
            let colIndex = 0;
            for (let i = 0; i < row.length; i++) {
                const char = row[i];
                
                if (isNaN(char)) {
                    // It's a piece
                    const tile = document.createElement('div');
                    tile.className = `tile ${(rowIndex + colIndex) % 2 === 0 ? 'light' : 'dark'}`;
                    tile.dataset.position = position;
                    
                    const pieceDiv = document.createElement('div');
                    pieceDiv.className = 'piece';
                    
                    const pieceType = this.getPieceTypeFromFen(char);
                    if (pieceType) {
                        pieceDiv.style.backgroundImage = `url('${this.pieceImages[pieceType]}')`;
                        tile.appendChild(pieceDiv);
                    }
                    
                    this.board.appendChild(tile);
                    position++;
                    colIndex++;
                } else {
                    // It's a number, add empty squares
                    const emptySquares = parseInt(char);
                    for (let j = 0; j < emptySquares; j++) {
                        const tile = document.createElement('div');
                        tile.className = `tile ${(rowIndex + colIndex) % 2 === 0 ? 'light' : 'dark'}`;
                        tile.dataset.position = position;
                        this.board.appendChild(tile);
                        position++;
                        colIndex++;
                    }
                }
            }
        });
    }

    getPieceTypeFromFen(char) {
        const pieceMap = {
            'P': 'WHITE_PAWN',
            'N': 'WHITE_KNIGHT',
            'B': 'WHITE_BISHOP',
            'R': 'WHITE_ROOK',
            'Q': 'WHITE_QUEEN',
            'K': 'WHITE_KING',
            'p': 'BLACK_PAWN',
            'n': 'BLACK_KNIGHT',
            'b': 'BLACK_BISHOP',
            'r': 'BLACK_ROOK',
            'q': 'BLACK_QUEEN',
            'k': 'BLACK_KING'
        };
        return pieceMap[char];
    }

    connectWebSocket() {
        const socket = new SockJS('/chess-websocket');
        this.stompClient = Stomp.over(socket);

        this.stompClient.connect({}, 
            (frame) => {
                console.log('Connected to WebSocket');
                this.connected = true;
                
                // Subscribe to game events
                this.stompClient.subscribe('/user/queue/game', (message) => {
                    try {
                        const gameEvent = JSON.parse(message.body);
                        console.log('Received game event:', gameEvent);
                        this.handleGameUpdate(gameEvent);
                    } catch (error) {
                        console.error('Error handling game event:', error);
                    }
                });

                // Subscribe to error messages
                this.stompClient.subscribe('/user/queue/errors', (message) => {
                    console.error('Received error:', message.body);
                });
            },
            (error) => {
                console.error('WebSocket connection error:', error);
                this.connected = false;
            }
        );
    }

    setupWebSocketUI() {
        const sendButton = document.getElementById('send-message');
            const messageInput = document.getElementById('message-input');
        
        if (!sendButton || !messageInput) {
            console.log('WebSocket UI elements not found, skipping setup');
            return;
        }

        sendButton.addEventListener('click', () => {
            const message = messageInput.value;
            if (message && this.stompClient) {
                this.stompClient.send("/app/message", {}, 
                    JSON.stringify({
                        content: message,
                        timestamp: new Date().toLocaleTimeString()
                    })
                );
                console.log('Message sent to server');
                messageInput.value = '';
            }
        });
    }

    initializeBoard() {
        if (this.currentGameState && this.currentGameState.fenPosition) {
            this.updateBoardFromFen(this.currentGameState.fenPosition);
        } else {
            // Default starting position in FEN
            this.updateBoardFromFen('rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1');
        }
    }

    placeInitialPieces() {
        const initialPositions = [
            'BLACK_ROOK', 'BLACK_KNIGHT', 'BLACK_BISHOP', 'BLACK_QUEEN', 'BLACK_KING', 'BLACK_BISHOP', 'BLACK_KNIGHT', 'BLACK_ROOK',
            'BLACK_PAWN', 'BLACK_PAWN', 'BLACK_PAWN', 'BLACK_PAWN', 'BLACK_PAWN', 'BLACK_PAWN', 'BLACK_PAWN', 'BLACK_PAWN',
            null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null,
            'WHITE_PAWN', 'WHITE_PAWN', 'WHITE_PAWN', 'WHITE_PAWN', 'WHITE_PAWN', 'WHITE_PAWN', 'WHITE_PAWN', 'WHITE_PAWN',
            'WHITE_ROOK', 'WHITE_KNIGHT', 'WHITE_BISHOP', 'WHITE_QUEEN', 'WHITE_KING', 'WHITE_BISHOP', 'WHITE_KNIGHT', 'WHITE_ROOK'
        ];

        const tiles = this.board.querySelectorAll('.tile');
        tiles.forEach((tile, index) => {
            const piece = initialPositions[index];
            if (piece) {
                const pieceDiv = document.createElement('div');
                pieceDiv.className = 'piece';
                pieceDiv.style.backgroundImage = `url('${this.pieceImages[piece]}')`;
                tile.appendChild(pieceDiv);
            }
        });
    }

    async handleTileClick(event) {
        const tile = event.target.closest('.tile');
        if (!tile) return;

        const position = parseInt(tile.dataset.position);

        // Clear previous selection first
        document.querySelector('.selected')?.classList.remove('selected');

        if (this.selectedSourceTile === null) {
            // First click - select piece
            if (this.hasPiece(tile)){
                const tileData = this.currentGameState.board.tiles.find(t => t.tileCoordinate === position);
                const piece = tileData.piece;
                    if(piece.pieceAlliance === this.currentGameState.board.currentPlayer.alliance){
                    this.selectedSourceTile = position;
                    tile.classList.add('selected');
                        this.showLegalMoves(position); // Show legal moves
                    }else{
                        document.querySelector('.selected')?.classList.remove('selected');
                        this.selectedSourceTile = null;
                        this.clearLegalMoves();
                        return;
                }
            }
        } else {
            if (this.hasPiece(tile)){
                const tileData = this.currentGameState.board.tiles.find(t => t.tileCoordinate === position);
                const piece = tileData.piece;
                if(piece.pieceAlliance === this.currentGameState.board.currentPlayer.alliance){
                    document.querySelector('.selected')?.classList.remove('selected');
                    this.selectedSourceTile = position;
                    tile.classList.add('selected');
                    this.clearLegalMoves();
                    this.showLegalMoves(position);
                    return;
                }
            }
            // Second click - make move
            this.clearLegalMoves(); // Clear previous highlights
            const sourceCoordinate = this.selectedSourceTile;
            const targetCoordinate = position;
            
            try {
                const response = await fetch(`/api/chess/move`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        sourceCoordinate,
                        targetCoordinate
                    })
                });
                
                if (!response.ok) {
                    throw new Error('Invalid move');
                }
                
                const gameState = await response.json();
                this.updateBoard(gameState);
                this.currentGameState = gameState;
                this.showLastMoveArrow(sourceCoordinate, targetCoordinate);
                this.highlightLastMove(sourceCoordinate, targetCoordinate);

                if (gameState.gameStatus === 'CHECKMATE') {
                    this.statusElement.textContent = 'Checkmate';
                } else if (gameState.gameStatus === 'DRAW') {
                    this.statusElement.textContent = gameState.drawType ? gameState.drawType.description : 'Draw';
                } else {
                    this.statusElement.textContent = 'Your turn';
                }
                
            } catch (error) {
                console.error('Error making move:', error);
                this.statusElement.textContent = 'Invalid move';
            }
            // Clear selection
            document.querySelector('.selected')?.classList.remove('selected');
            this.selectedSourceTile = null;
        }
    }

    handleMouseDown(event) {
        const piece = event.target.closest('.piece');
        if (!piece) return;

        const tile = piece.parentElement;
        const position = parseInt(tile.dataset.position);
        const tileData = this.currentGameState.board.tiles.find(t => t.tileCoordinate === position);
        
        if (!tileData || !tileData.piece) return;

        const pieceData = tileData.piece;
        if (pieceData.pieceAlliance !== this.currentGameState.board.currentPlayer.alliance) return;

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
                    const response = await fetch(`/api/chess/move`, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify({
                            sourceCoordinate: this.selectedSourceTile,
                            targetCoordinate: targetPosition
                        })
                    });
                    
                    if (!response.ok) {
                        throw new Error('Invalid move');
                    }
                    
                    const gameState = await response.json();
                    this.updateBoard(gameState);
                    this.currentGameState = gameState;
                    this.showLastMoveArrow(this.selectedSourceTile, targetPosition);
                    this.highlightLastMove(this.selectedSourceTile, targetPosition);

                    if (gameState.gameStatus === 'CHECKMATE') {
                        this.statusElement.textContent = 'Checkmate';
                    } else if (gameState.gameStatus === 'DRAW') {
                        this.statusElement.textContent = gameState.drawType ? gameState.drawType.description : 'Draw';
                    } else {
                        this.statusElement.textContent = 'Your turn';
                    }
                    
                } catch (error) {
                    console.error('Error making move:', error);
                    this.statusElement.textContent = 'Invalid move';
                } finally {
                    // Ensure dragover effects are cleaned up after move attempt
                    document.querySelectorAll('.tile.dragover').forEach(tile => {
                        tile.classList.remove('dragover');
                    });
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
        this.selectedSourceTile = null;
        this.clearLegalMoves();
        document.querySelector('.selected')?.classList.remove('selected');

        event.preventDefault();
    }

    showLegalMoves(position) {
        // Assuming gameState is an object that includes the current legal moves
        const legalMoves = this.currentGameState.board.currentPlayer.moves; // Ensure this is populated when fetching game state
    
        legalMoves.forEach(move => {
            if (move.sourceCoordinate === position) {
                const tile = this.board.querySelector(`.tile[data-position='${move.targetCoordinate}']`);
                if (tile) {
                    const piece = tile.querySelector('.piece');
                    if(piece){
                        tile.classList.add('legal-move-capture');
                    }else{
                        tile.classList.add('legal-move-non-capture');
                    }
                }
            }
        });
    }

    clearLegalMoves() {
        const legalMoveTiles = this.board.querySelectorAll('.legal-move-capture, .legal-move-non-capture');
        legalMoveTiles.forEach(tile => {
            tile.classList.remove('legal-move-capture', 'legal-move-non-capture');
        });
    }

    hasPiece(tile) {
        return tile.querySelector('.piece') !== null;
    }

    updateBoard(gameState) {
        const tiles = this.board.querySelectorAll('.tile');
        tiles.forEach((tile, index) => {
            tile.innerHTML = '';
            const tileData = gameState.board.tiles.find(t => t.tileCoordinate === index);
            if (tileData && tileData.tileOccupied) {
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

        // // Clear previous arrow
        // if (this.lastMoveArrow) {
        //     this.lastMoveArrow.remove();
        //     this.lastMoveArrow = null;
        // }
        // document.querySelector('.last-move-arrow')?.remove();

        // Add highlights to source and target tiles
        const sourceTile = this.board.querySelector(`.tile[data-position='${sourcePos}']`);
        const targetTile = this.board.querySelector(`.tile[data-position='${targetPos}']`);
        
        if (sourceTile) sourceTile.classList.add('last-move-source');
        if (targetTile) targetTile.classList.add('last-move-target');
    }
}

// Initialize the game when the page loads
document.addEventListener('DOMContentLoaded', () => {
    const gameBoard = document.getElementById('game-board');
    if (gameBoard) {
        // Check if we have game data from the server
        const gameDataElement = document.getElementById('gameData');
        let gameData = null;
        
        if (gameDataElement) {
            try {
                gameData = JSON.parse(gameDataElement.textContent);
                console.log('Initializing with server game data:', gameData);
            } catch (error) {
                console.error('Error parsing game data:', error);
            }
        }
        
        window.chessGame = new ChessGame(gameData);
    }
}); 