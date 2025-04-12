class ChessGame {
    constructor() {
        console.log('Initializing chess game...');
        this.board = document.getElementById('game-board');
        if (!this.board) {
            console.error('Could not find game board element');
            return;
        }
        console.log('Found game board, initializing...');
        this.piece = document.getElementById('piece');
        this.statusElement = document.getElementById('status');
        this.boardDTO = null;
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
        
        this.initializeBoard();
        this.setupEventListeners();
        this.initializeArrowMarker();
        this.gameId= null;
        this.stompClient = null;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        console.log('Board initialized');
    }

    initializeBoard() {
        console.log('Setting up board...');
        this.board.innerHTML = '';
        for (let row = 0; row < 8; row++) {
            for (let col = 0; col < 8; col++) {
                const tile = document.createElement('div');
                tile.className = `tile ${(row + col) % 2 === 0 ? 'light' : 'dark'}`;
                tile.dataset.position = row * 8 + col;
                this.board.appendChild(tile);
            }
        }
        this.placeInitialPieces();
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

    setupEventListeners() {
        document.getElementById('new-game').addEventListener('click', () => this.startNewGame());
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

    async startNewGame() {
        console.log('Starting new game...');
        
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
        this.selectedTargetTile = null;
        this.isDragging = false;
        this.draggedPiece = null;
        this.dragImage = null;
        this.lastMoveArrow = null;
        this.boardDTO = null;
        this.connectWebSocket();
    }

    connectWebSocket() {
        console.log('Connecting to WebSocket...');
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
                this.stompClient.subscribe('/topic/game/' + this.gameId, (message) => {
                    console.log('Received WebSocket message for game ID:', this.gameId, 'Message:', message.body);
                    
                    try {
                        // Parse the message body as JSON
                        const data = JSON.parse(message.body);
                        console.log('Parsed WebSocket message:', data);
                        
                        // Handle different message types
                        if (data.type === 'GAME_STARTED') {
                            console.log('Game started notification received');
                            this.boardDTO = data.boardDTO;
                            this.updateBoard();
                            this.statusElement.textContent = 'Game in progress - Your turn';
                        } else if (data.type === 'MOVE_MADE') {
                            console.log('Move made notification received');
                            
                            // If moveResult is included in the message, use it to update the board
                            if (data.moveResult) {
                                const moveResult = data.moveResult;
                                console.log('Move result received:', moveResult);
                                
                                this.boardDTO = moveResult.board;
                                // Update the board with the new game state
                                this.updateBoard();
                                
                                // Update status message based on game status
                                if (moveResult.gameDTO.status === 'CHECKMATE') {
                                    this.statusElement.textContent = 'Checkmate';
                                } else if (moveResult.gameDTO.status === 'DRAW') {
                                    this.statusElement.textContent = moveResult.gameDTO.drawType ? moveResult.gameDTO.drawType.description : 'Draw';
                                } else {
                                    this.statusElement.textContent = 'Your turn';
                                }
                            } else {
                                // Fallback to refreshing the game state if moveResult is not included
                                this.refreshGameState();
                            }
                        }
                    } catch (error) {
                        console.error('Error parsing WebSocket message:', error);
                    }
                });
    
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
        const tile = event.target.closest('.tile');
        if (!tile) return;

        const position = parseInt(tile.dataset.position);
        console.log('Tile clicked at position:', position);

        // Clear previous selection first
        document.querySelector('.selected')?.classList.remove('selected');

        if (this.selectedSourceTile === null) {
            // First click - select piece
            if (this.hasPiece(tile)) {
                const tileData = this.boardDTO.tiles.find(t => t.tileCoordinate === position);
                console.log('Tile data:', tileData);
                
                if (!tileData || !tileData.piece) {
                    console.error('No piece data found for tile at position:', position);
                    return;
                }
                
                const piece = tileData.piece;
                console.log('Piece at position:', piece);
                
                if (piece.pieceAlliance === this.boardDTO.currentPlayer.alliance) {
                    console.log('Piece belongs to current player, selecting...');
                    this.selectedSourceTile = position;
                    tile.classList.add('selected');
                    this.showLegalMoves(position); // Show legal moves
                } else {
                    console.log('Piece does not belong to current player, ignoring...');
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
                    console.log('Selecting different piece of same color');
                    document.querySelector('.selected')?.classList.remove('selected');
                    this.selectedSourceTile = position;
                    tile.classList.add('selected');
                    this.clearLegalMoves();
                    this.showLegalMoves(position);
                    return;
                }
            }
            
            // Check if the target position is a legal move
            const isLegalMove = this.boardDTO.currentPlayer.moves.some(
                move => move.sourceCoordinate === this.selectedSourceTile && move.targetCoordinate === position
            );
            
            if (isLegalMove) {
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
                    
                    if (!response.ok) {
                        const errorText = await response.text();
                        console.error('Move failed:', response.status, errorText);
                        throw new Error('Invalid move');
                    }
                    
                    // Get the response data
                    const moveResult = await response.json();
                    console.log('Move result received:', moveResult);
                    
                    // We'll let the WebSocket message handle the board update
                    // to avoid race conditions
                    
                    this.showLastMoveArrow(this.selectedSourceTile, position);
                    this.highlightLastMove(this.selectedSourceTile, position);
                } catch (error) {
                    console.error('Error making move:', error);
                    this.statusElement.textContent = 'Invalid move';
                } finally {
                    // Ensure dragover effects are cleaned up after move attempt
                    document.querySelectorAll('.tile.dragover').forEach(tile => {
                        tile.classList.remove('dragover');
                    });
                }
            } else {
                console.log('Not a legal move, ignoring...');
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
                    
                    // We'll let the WebSocket message handle the board update
                    // to avoid race conditions
                    
                    this.showLastMoveArrow(this.selectedSourceTile, targetPosition);
                    this.highlightLastMove(this.selectedSourceTile, targetPosition);
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
        // Log the position being checked and current board state
        console.log('Showing legal moves for position:', position);
        console.log('Current board state:', this.boardDTO);
        
        // Check if boardDTO and currentPlayer exist
        if (!this.boardDTO || !this.boardDTO.currentPlayer) {
            console.error('Board data or current player is missing');
            return;
        }
        
        // Get the moves array from currentPlayer
        const moves = this.boardDTO.currentPlayer.moves;
        console.log('Moves:', moves);
        
        // Validate moves array
        if (!moves || !Array.isArray(moves)) {
            console.error('Moves array is missing or invalid');
            return;
        }
        
        // Filter moves for the selected position
        const legalMoves = moves.filter(move => move.sourceCoordinate === position);
        console.log('Legal moves:', legalMoves);
        // Highlight each legal move
        legalMoves.forEach(move => {
            const targetTile = this.board.querySelector(`.tile[data-position='${move.targetCoordinate}']`);
            console.log('Target tile:', targetTile);
            if (targetTile) {
                // Check if the move is a capture
                const targetTileData = this.boardDTO.tiles.find(t => t.tileCoordinate === move.targetCoordinate);
                console.log('Target tile data:', targetTileData);
                const isCapture = targetTileData && targetTileData.tileOccupied;
                console.log('Is capture:', isCapture);
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
        console.log('Updating board with data:', this.boardDTO);
        
        // Check if boardDTO exists and has the expected structure
        if (!this.boardDTO) {
            console.error('Board data is missing or invalid');
            return;
        }
        
        const tiles = this.board.querySelectorAll('.tile');
        tiles.forEach((tile, index) => {
            tile.innerHTML = '';
            
            // Check if the board has tiles property
            if (!this.boardDTO.tiles || !Array.isArray(this.boardDTO.tiles)) {
                console.error('Board data does not have tiles array');
                return;
            }
            
            const tileData = this.boardDTO.tiles.find(t => t.tileCoordinate === index);
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
                console.log('Starting game automatically...');
                window.chessGame.startNewGame();
            }
        } else {
            console.error('Could not find game ID in URL path');
        }
    } else {
        console.error('Could not find game board element');
    }
}); 