class ChessGame {
    constructor() {
        this.board = document.getElementById('game-board');
        this.statusElement = document.getElementById('status');
        this.selectedTile = null;
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

        if (this.selectedTile === null) {
            // First click - select piece
            if (this.hasPiece(tile)) {
                this.selectedTile = position;
                tile.classList.add('selected');
            }
        } else {
            // Second click - make move
            const sourceCoordinate = this.selectedTile;
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
            this.selectedTile = null;
        }
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
                    console.log('Piece at', index, ':', pieceKey, 'Image:', this.pieceImages[pieceKey]); // Debug log
                    const pieceElement = document.createElement('div');
                    pieceElement.className = 'piece';
                    pieceElement.style.backgroundImage = `url('${this.pieceImages[pieceKey]}')`;
                    tile.appendChild(pieceElement);
                }
            }
        });
    }
}

// Initialize the game when the page loads
document.addEventListener('DOMContentLoaded', () => {
    new ChessGame();
}); 