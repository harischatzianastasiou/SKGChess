/**
 * Game UI Handler
 * This file handles the UI interactions for the chess game page
 */

// Wait for the DOM to be fully loaded
document.addEventListener('DOMContentLoaded', function() {
    // Initialize UI elements
    initGameUI();
    
    // Set up event listeners for UI controls
    setupUIEventListeners();
});

/**
 * Initialize the game UI elements
 */
function initGameUI() {
    // Get references to UI elements
    const resignBtn = document.getElementById('resign-btn');
    const drawBtn = document.getElementById('draw-btn');
    const newGameBtn = document.getElementById('new-game-btn');
    const sendMessageBtn = document.getElementById('send-message-btn');
    const chatInput = document.getElementById('chat-input');
    
    // Initialize game status
    updateGameStatus('Waiting for opponent');
    
    // Initialize turn indicator
    updateTurnIndicator('White');
}

/**
 * Set up event listeners for UI controls
 */
function setupUIEventListeners() {
    // Resign button
    const resignBtn = document.getElementById('resign-btn');
    if (resignBtn) {
        resignBtn.addEventListener('click', function() {
            if (confirm('Are you sure you want to resign the game?')) {
                resignGame();
            }
        });
    }
    
    // Draw button
    const drawBtn = document.getElementById('draw-btn');
    if (drawBtn) {
        drawBtn.addEventListener('click', function() {
            offerDraw();
        });
    }
    
    // New game button
    const newGameBtn = document.getElementById('new-game-btn');
    if (newGameBtn) {
        newGameBtn.addEventListener('click', function() {
            window.location.href = '/play';
        });
    }
    
    // Send message button
    const sendMessageBtn = document.getElementById('send-message-btn');
    const chatInput = document.getElementById('chat-input');
    
    if (sendMessageBtn && chatInput) {
        sendMessageBtn.addEventListener('click', function() {
            sendChatMessage();
        });
        
        // Allow sending message with Enter key
        chatInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                sendChatMessage();
            }
        });
    }
}

/**
 * Update the game status display
 * @param {string} status - The new game status
 */
function updateGameStatus(status) {
    const statusElement = document.getElementById('game-status-value');
    if (statusElement) {
        statusElement.textContent = status;
    }
}

/**
 * Update the turn indicator
 * @param {string} turn - The current turn (White or Black)
 */
function updateTurnIndicator(turn) {
    const turnElement = document.getElementById('current-turn');
    if (turnElement) {
        turnElement.textContent = turn;
    }
}

/**
 * Add a move to the move history
 * @param {string} move - The move notation (e.g., "e4")
 * @param {string} player - The player who made the move
 */
function addMoveToHistory(move, player) {
    const movesContainer = document.getElementById('moves-container');
    if (movesContainer) {
        const moveElement = document.createElement('div');
        moveElement.className = 'move-entry';
        moveElement.innerHTML = `<span class="move-player">${player}:</span> <span class="move-notation">${move}</span>`;
        movesContainer.appendChild(moveElement);
        
        // Scroll to the bottom of the move history
        movesContainer.scrollTop = movesContainer.scrollHeight;
    }
}

/**
 * Add a chat message to the chat panel
 * @param {string} message - The chat message
 * @param {string} sender - The sender of the message
 * @param {boolean} isCurrentUser - Whether the message is from the current user
 */
function addChatMessage(message, sender, isCurrentUser) {
    const chatMessages = document.getElementById('chat-messages');
    if (chatMessages) {
        const messageElement = document.createElement('div');
        messageElement.className = `chat-message ${isCurrentUser ? 'current-user' : 'other-user'}`;
        
        const senderElement = document.createElement('div');
        senderElement.className = 'message-sender';
        senderElement.textContent = sender;
        
        const contentElement = document.createElement('div');
        contentElement.className = 'message-content';
        contentElement.textContent = message;
        
        messageElement.appendChild(senderElement);
        messageElement.appendChild(contentElement);
        chatMessages.appendChild(messageElement);
        
        // Scroll to the bottom of the chat
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }
}

/**
 * Send a chat message
 */
function sendChatMessage() {
    const chatInput = document.getElementById('chat-input');
    if (chatInput && chatInput.value.trim() !== '') {
        const message = chatInput.value.trim();
        const username = document.getElementById('username').textContent;
        
        // Add the message to the chat panel
        addChatMessage(message, username, true);
        
        // Clear the input field
        chatInput.value = '';
        
        // TODO: Send the message to the server via WebSocket
        // This would be implemented when the backend supports chat functionality
    }
}

/**
 * Resign the game
 */
function resignGame() {
    // TODO: Implement resign functionality
    // This would send a resign message to the server via WebSocket
    alert('Resign functionality will be implemented in a future update.');
}

/**
 * Offer a draw to the opponent
 */
function offerDraw() {
    // TODO: Implement draw offer functionality
    // This would send a draw offer to the server via WebSocket
    alert('Draw offer functionality will be implemented in a future update.');
}

/**
 * Copy the game ID to the clipboard
 */
function copyGameId() {
    const gameId = document.querySelector('.game-id').textContent;
    navigator.clipboard.writeText(gameId).then(() => {
        const copyBtn = document.querySelector('.copy-btn');
        copyBtn.innerHTML = '<i class="fas fa-check"></i>';
        setTimeout(() => {
            copyBtn.innerHTML = '<i class="fas fa-copy"></i>';
        }, 2000);
    });
} 