class ChessGame {
    constructor() {
        this.board = document.getElementById('game-board');
        this.piece = document.getElementById('piece');
        this.statusElement = document.getElementById('status');
        this.currentGameState = null;
        this.selectedSourceTile = null;
        this.selectedTargetTile = null;
        this.isDragging = false;
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
                pieceDiv.setAttribute('draggable', true);
                pieceDiv.addEventListener('dragstart', this.handleDragStart.bind(this));
                tile.appendChild(pieceDiv);
            }
        });
    }

    setupEventListeners() {
        document.getElementById('new-game').addEventListener('click', () => this.startNewGame());
        this.board.addEventListener('click', (e) => this.handleTileClick(e));
        
        // Add drag and drop listeners to the board
        this.board.addEventListener('dragstart', this.handleDragStart.bind(this));
        this.board.addEventListener('dragover', this.handleDragOver.bind(this));
        this.board.addEventListener('drop', this.handleDrop.bind(this));
        this.board.addEventListener('dragend', this.handleDragEnd.bind(this));
        this.board.addEventListener('dragenter', this.handleDragEnter.bind(this));
        this.board.addEventListener('dragleave', this.handleDragLeave.bind(this));

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

    handleDragStart(event) {
        this.isDragging = true;
        document.body.classList.add('grabbing');
        // Clear any previous selections first
        document.querySelector('.selected')?.classList.remove('selected');
        this.clearLegalMoves();
        
        const piece = event.target.closest('.piece');
        if (!piece) return;

        const tile = piece.parentElement;
        const position = parseInt(tile.dataset.position);
        const tileData = this.currentGameState.board.tiles.find(t => t.tileCoordinate === position);
        
        if (!tileData || !tileData.piece) {
            event.preventDefault();
            return;
        }

        const pieceData = tileData.piece;
        if (pieceData.pieceAlliance !== this.currentGameState.board.currentPlayer.alliance) {
            event.preventDefault();
            return;
        }

        this.selectedSourceTile = position;
        event.dataTransfer.setData('text/plain', position);
        event.dataTransfer.effectAllowed = 'move';
        
        // Add dragging class for visual feedback
        piece.classList.add('dragging');
        
        // Highlight the source tile
        const sourceTile = this.board.querySelector(`.tile[data-position='${position}']`);
        sourceTile.classList.add('selected');
        
        // Add a slight delay to show legal moves after drag starts
        setTimeout(() => {
            this.showLegalMoves(position);
        }, 50);
    }

    handleDragOver(event) {
        event.preventDefault();
        event.dataTransfer.dropEffect = 'move';
    }

    handleDragEnter(event) {
        const tile = event.target.closest('.tile');
        if (tile) {
            tile.classList.add('dragover');
        }
    }

    handleDragLeave(event) {
        const tile = event.target.closest('.tile');
        if (tile) {
            tile.classList.remove('dragover');
        }
    }

    handleDragEnd(event) {
        this.isDragging = false;
        document.body.classList.remove('grabbing');
        const pieces = document.querySelectorAll('.piece');
        pieces.forEach(piece => piece.classList.remove('dragging'));
        
        // Remove dragover class from all tiles
        document.querySelectorAll('.tile').forEach(tile => {
            tile.classList.remove('dragover');
        });
        
        // Reset the piece to its original position if the drag was not successful
        if (this.selectedSourceTile !== null) {
            const sourceTile = this.board.querySelector(`.tile[data-position='${this.selectedSourceTile}']`);
            const piece = document.querySelector('.dragging');
            if (piece && sourceTile) {
                sourceTile.appendChild(piece);
            }
            this.clearLegalMoves();
            document.querySelector('.selected')?.classList.remove('selected');
            this.selectedSourceTile = null;
        }
    }

    async handleDrop(event) {
        event.preventDefault();
        
        const tile = event.target.closest('.tile');
        if (!tile) {
            this.clearLegalMoves();
            document.querySelector('.selected')?.classList.remove('selected');
            this.selectedSourceTile = null;
            return;
        }

        const targetPosition = parseInt(tile.dataset.position);
        const sourcePosition = this.selectedSourceTile;

        if (sourcePosition === null || sourcePosition === targetPosition) {
            this.clearLegalMoves();
            document.querySelector('.selected')?.classList.remove('selected');
            this.selectedSourceTile = null;
            return;
        }

        try {
            const response = await fetch(`/api/chess/move`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    sourceCoordinate: sourcePosition,
                    targetCoordinate: targetPosition
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
        } finally {
            // Clear selection and legal moves after the move is processed
            this.selectedSourceTile = null;
            this.clearLegalMoves();
            document.querySelector('.selected')?.classList.remove('selected');
        }
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
                    pieceElement.setAttribute('draggable', true);
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