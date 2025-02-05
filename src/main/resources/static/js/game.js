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
        this.currentGameState = null;
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

        try {
            const response = await fetch('/api/chess/create', { method: 'POST' });
    
            if (!response.ok) {
                throw new Error('Failed to start a new game');
            }
    
            const data = await response.json(); 
            const message = data.message;
    
            const gameResponse = await fetch(`/api/chess/read`);

            if (!gameResponse.ok) {
                throw new Error('Failed to fetch game state');
            }

            const gameState = await gameResponse.json();
    
            this.updateBoard(gameState);
            this.currentGameState = gameState;
            this.statusElement.textContent = 'Game started! Your turn (White)';
        } catch (error) {
            console.error('Error starting new game:', error);
            this.statusElement.textContent = 'Error starting game';
        }
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
        // Initialize the chess game
        window.chessGame = new ChessGame();
        
        // Get game ID if available
        const gameId = document.getElementById('gameId')?.textContent;
        if (gameId) {
            console.log('Game initialized with ID:', gameId);
            // You can use this ID to fetch game state from server
        }
    } else {
        console.error('Could not find game board element');
    }
}); 