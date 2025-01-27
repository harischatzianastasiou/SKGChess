class ChessGame {
    constructor() {
        this.board = document.getElementById('game-board');
        this.piece = document.getElementById('piece');
        this.statusElement = document.getElementById('status');
        this.currentGameState = null;
        this.selectedSourceTile = null;
        this.selectedTargetTile = null;
        this.isDragging = false;
        this.draggedPiece = null;
        this.dragImage = null;
        this.pieceImages = {
            'WHITE_PAWN': 'images/white_p.png',
            'WHITE_KNIGHT': 'images/white_n.png',
            'WHITE_BISHOP': 'images/white_b.png',
            'WHITE_ROOK': 'images/white_r.png',
            'WHITE_QUEEN': 'images/white_q.png',
            'WHITE_KING': 'images/white_k.png',
            'BLACK_PAWN': 'images/black_p.png',
            'BLACK_KNIGHT': 'images/black_n.png',
            'BLACK_BISHOP': 'images/black_b.png',
            'BLACK_ROOK': 'images/black_r.png',
            'BLACK_QUEEN': 'images/black_q.png',
            'BLACK_KING': 'images/black_k.png'
        };
        
        this.initializeBoard();
        this.setupEventListeners();
    }

    initializeBoard() {
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

        // Clear any previous selections first
        this.clearLegalMoves();
        document.querySelector('.selected')?.classList.remove('selected');

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

        // Show legal moves and highlight selected tile
        tile.classList.add('selected');
        this.showLegalMoves(position);

        event.preventDefault();
    }

    handleMouseMove(event) {
        if (!this.isDragging || !this.dragImage) return;
        
        this.dragImage.style.left = (event.clientX - 30) + 'px';
        this.dragImage.style.top = (event.clientY - 30) + 'px';
        
        event.preventDefault();
    }

    async handleMouseUp(event) {
        if (!this.isDragging) return;

        const targetTile = event.target.closest('.tile');
        let moveSuccessful = false;

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
                    moveSuccessful = true;
                    this.updateBoard(gameState);
                    this.currentGameState = gameState;

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
            }
        }

        // Cleanup
        if (this.draggedPiece) {
            this.draggedPiece.style.opacity = '1';
            
            // Add landing animation if move was not successful
            if (!moveSuccessful) {
                this.draggedPiece.style.transition = 'all 0.08s ease-out';
                this.draggedPiece.style.transform = 'scale(0.97)';
                requestAnimationFrame(() => {
                    this.draggedPiece.style.transform = 'scale(1)';
                    this.draggedPiece.animate([
                        { transform: 'scale(1) translateX(-1px)' },
                        { transform: 'scale(1) translateX(1px)' },
                        { transform: 'scale(1) translateX(0)' }
                    ], {
                        duration: 50,
                        easing: 'ease-out',
                        iterations: 1
                    });
                });
            }
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
            const existingPiece = tile.querySelector('.piece');
            const tileData = gameState.board.tiles.find(t => t.tileCoordinate === index);
            
            if (tileData && tileData.tileOccupied && tileData.piece) {
                const pieceKey = tileData.piece.pieceAlliance + '_' + tileData.piece.pieceSymbol;
                
                if (existingPiece) {
                    // Update existing piece if it's different
                    if (existingPiece.style.backgroundImage !== `url('${this.pieceImages[pieceKey]}')`) {
                        existingPiece.style.backgroundImage = `url('${this.pieceImages[pieceKey]}')`;
                    }
                } else {
                    // Create new piece with transition
                    const pieceElement = document.createElement('div');
                    pieceElement.className = 'piece';
                    pieceElement.style.backgroundImage = `url('${this.pieceImages[pieceKey]}')`;
                    pieceElement.style.transition = 'all 0.15s ease-out';
                    pieceElement.style.opacity = '0';
                    pieceElement.style.transform = 'scale(0.95)';
                    tile.appendChild(pieceElement);
                    
                    // Trigger landing animation in next frame
                    requestAnimationFrame(() => {
                        pieceElement.style.opacity = '1';
                        pieceElement.style.transform = 'scale(1)';
                        
                        // Add subtle shake animation
                        pieceElement.animate([
                            { transform: 'scale(1) translateX(-1px)' },
                            { transform: 'scale(1) translateX(1px)' },
                            { transform: 'scale(1) translateX(0)' }
                        ], {
                            duration: 100,
                            easing: 'ease-out',
                            iterations: 1
                        });
                    });
                }
            } else if (existingPiece) {
                // Remove piece with subtle fade out
                existingPiece.style.transition = 'all 0.1s ease-out';
                existingPiece.style.opacity = '0';
                existingPiece.style.transform = 'scale(0.95)';
                setTimeout(() => existingPiece.remove(), 100);
            }
        });
    }
}

// Initialize the game when the page loads
document.addEventListener('DOMContentLoaded', () => {
    new ChessGame();
}); 