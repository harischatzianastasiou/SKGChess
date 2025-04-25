// Modal functionality for login popup
document.addEventListener('DOMContentLoaded', function() {
    // Create modal HTML structure if it doesn't exist
    if (!document.getElementById('login-modal')) {
        createLoginModal();
    }
    
    // Add event listeners for modal open/close
    setupModalEventListeners();
    
    // Add event listener for Escape key
    document.addEventListener('keydown', function(event) {
        if (event.key === 'Escape') {
            closeModal();
        }
    });
});

/**
 * Creates the login modal HTML structure
 */
function createLoginModal() {
    // Create modal HTML
    const modalHTML = `
        <div id="login-modal" class="modal-overlay">
            <div class="modal-container">
                <div class="modal-header">
                    <h3 class="modal-title">Login to Play</h3>
                    <button class="modal-close">&times;</button>
                </div>
                <div class="modal-body">
                    <form id="login-form" class="login-form" action="/login" method="POST">
                        <div class="form-group">
                            <label for="modal-username">Username</label>
                            <input type="text" id="modal-username" name="username" required>
                        </div>
                        <div class="form-group">
                            <label for="modal-password">Password</label>
                            <input type="password" id="modal-password" name="password" required>
                        </div>
                        <div class="remember-me">
                            <input type="checkbox" id="modal-remember" name="remember">
                            <label for="modal-remember">Remember Me</label>
                        </div>
                        <button type="submit" class="login-btn">Log In</button>
                        <p class="signup-prompt">Don't have an account? <a href="/signup">Sign up here</a></p>
                    </form>
                </div>
            </div>
        </div>
    `;
    
    // Append modal to body
    document.body.insertAdjacentHTML('beforeend', modalHTML);
}

/**
 * Sets up event listeners for the modal
 */
function setupModalEventListeners() {
    const modal = document.getElementById('login-modal');
    const closeBtn = modal.querySelector('.modal-close');
    const loginForm = document.getElementById('login-form');
    
    // Close modal when clicking the close button
    closeBtn.addEventListener('click', function() {
        closeModal();
    });
    
    // Close modal when clicking outside the modal content
    modal.addEventListener('click', function(event) {
        if (event.target === modal) {
            closeModal();
        }
    });
    
    // Handle form submission
    loginForm.addEventListener('submit', function(event) {
        event.preventDefault();
        
        // Get form data
        const username = document.getElementById('modal-username').value;
        const password = document.getElementById('modal-password').value;
        const remember = document.getElementById('modal-remember').checked;
        
        // Submit form data to server
        submitLoginForm(username, password, remember);
    });
}

/**
 * Opens the login modal
 */
function openLoginModal() {
    const modal = document.getElementById('login-modal');
    modal.classList.add('active');
    
    // Focus on username input
    setTimeout(() => {
        document.getElementById('modal-username').focus();
    }, 100);
    
    // Prevent body scrolling
    document.body.style.overflow = 'hidden';
}

/**
 * Closes the login modal
 */
function closeModal() {
    const modal = document.getElementById('login-modal');
    modal.classList.remove('active');
    
    // Restore body scrolling
    document.body.style.overflow = '';
    
    // Clear the pending action when modal is closed
    sessionStorage.removeItem('pendingAction');
}

/**
 * Submits the login form data to the server
 * @param {string} username - The username
 * @param {string} password - The password
 * @param {boolean} remember - Whether to remember the user
 */
function submitLoginForm(username, password, remember) {
    // Create form data
    const formData = new FormData();
    formData.append('username', username);
    formData.append('password', password);
    if (remember) {
        formData.append('remember', 'on');
    }
    
    // Submit form data to server
    fetch('/login', {
        method: 'POST',
        body: formData,
        credentials: 'same-origin' // Include cookies
    })
    .then(response => {
        if (response.ok) {
            // Get the pending action
            const pendingAction = sessionStorage.getItem('pendingAction');
            
            if (pendingAction === 'newGame') {
                // For new game, create the game directly without reloading the page
                createGameDirectly(username);
            } else {
                // For other actions, reload the page
                window.location.reload();
            }
        } else {
            // Login failed, show error
            return response.text().then(text => {
                throw new Error(text || 'Login failed');
            });
        }
    })
    .catch(error => {
        // Show error message
        alert('Login failed: ' + error.message);
    });
}

/**
 * Creates a new game directly after login
 * @param {string} username - The username of the logged-in user
 */
function createGameDirectly(username) {
    // Create the request body
    const requestBody = {
        username: username
    };
    
    // Call the create game endpoint
    fetch('/api/games', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestBody)
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(text => {
                throw new Error(`Failed to create game: ${response.status} ${response.statusText}. ${text}`);
            });
        }
        return response.json();
    })
    .then(data => {
        if (!data || !data.id) {
            throw new Error('Game ID not found in response');
        }
        
        // Clear the pending action
        sessionStorage.removeItem('pendingAction');
        
        // Redirect to the game page
        window.location.href = `/games/${data.id}`;
    })
    .catch(error => {
        console.error("Error creating game:", error);
        alert(`Error creating game: ${error.message}`);
        
        // Clear the pending action
        sessionStorage.removeItem('pendingAction');
    });
}

/**
 * Shows the login modal when a user tries to access a protected feature
 * @param {string} action - The action the user is trying to perform
 */
function showLoginForAction(action) {
    // Store the action in sessionStorage to handle after login
    sessionStorage.setItem('pendingAction', action);
    
    // Open the login modal
    openLoginModal();
}

/**
 * Handles the pending action after successful login
 */
function handlePendingAction() {
    const pendingAction = sessionStorage.getItem('pendingAction');
    
    if (pendingAction) {
        // Clear the pending action
        sessionStorage.removeItem('pendingAction');
        
        // Handle the action based on its type
        switch (pendingAction) {
            case 'newGame':
                newGame();
                break;
            case 'joinGame':
                showJoinGameDialog();
                break;
            default:
                console.log('Unknown pending action:', pendingAction);
        }
    }
}

// Export functions for use in other scripts
window.openLoginModal = openLoginModal;
window.showLoginForAction = showLoginForAction;
window.handlePendingAction = handlePendingAction; 