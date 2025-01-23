class ChessGame {
    constructor() {
        this.board = document.getElementById('game-board');
        this.statusElement = document.getElementById('status');
        this.selectedTile = null;
        this.sessionId = null;
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
    }

    setupEventListeners() {
        document.getElementById('new-game').addEventListener('click', () => this.startNewGame());
        this.board.addEventListener('click', (e) => this.handleTileClick(e));
    }

    async startNewGame() {
        try {
            const response = await fetch('/api/chess/new-game', {
                method: 'POST'
            });
            const gameState = await response.json();
            this.sessionId = gameState.sessionId;
            this.updateBoard(gameState);
            this.statusElement.textContent = 'Game started! Your turn (White)';
        } catch (error) {
            console.error('Error starting new game:', error);
            this.statusElement.textContent = 'Error starting game';
        }
    }

    async handleTileClick(event) {
        if (!this.sessionId) return;

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
                const response = await fetch(`/api/chess/${this.sessionId}/move`, {
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
                
                if (gameState.isGameOver) {
                    this.statusElement.textContent = gameState.gameResult;
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
        console.log('Updating board with game state:', gameState); // Debug log
        const tiles = this.board.querySelectorAll('.tile');
        tiles.forEach((tile, index) => {
            tile.innerHTML = '';
            const piece = gameState.pieces[index];
            if (piece) {
                const pieceKey = piece.alliance + '_' + piece.type;
                console.log('Piece at', index, ':', pieceKey, 'Image:', this.pieceImages[pieceKey]); // Debug log
                const pieceDiv = document.createElement('div');
                pieceDiv.className = 'piece';
                pieceDiv.style.backgroundImage = `url('${this.pieceImages[pieceKey]}')`;
                tile.appendChild(pieceDiv);
            }
        });
    }
}

// Initialize the game when the page loads
document.addEventListener('DOMContentLoaded', () => {
    new ChessGame();
}); 