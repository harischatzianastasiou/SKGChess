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
 * Creates the signup form HTML structure
 */
function createSignupForm() {
    return `
        <form id="signup-form" class="login-form" action="/signup" method="POST">
            <div class="form-group">
                <label for="modal-signup-username">Username</label>
                <input type="text" id="modal-signup-username" name="username" placeholder="Choose a username" required>
            </div>
            <div class="form-group">
                <label for="modal-signup-email">Email</label>
                <input type="email" id="modal-signup-email" name="email" placeholder="Enter your email" required>
            </div>
            <div class="form-group">
                <label for="modal-signup-password">Password</label>
                <div class="password-input">
                    <input type="password" id="modal-signup-password" name="password" placeholder="Create a password" required>
                    <button type="button" class="toggle-password" onclick="toggleSignupPasswordVisibility()">
                        <img src="/images/eye.svg" alt="Toggle password visibility">
                    </button>
                </div>
            </div>
            <div class="form-group">
                <label for="modal-signup-confirm-password">Confirm Password</label>
                <div class="password-input">
                    <input type="password" id="modal-signup-confirm-password" name="confirmPassword" placeholder="Confirm your password" required>
                    <button type="button" class="toggle-password" onclick="toggleSignupConfirmPasswordVisibility()">
                        <img src="/images/eye.svg" alt="Toggle password visibility">
                    </button>
                </div>
            </div>
            <button type="submit" class="login-btn">Sign Up</button>
        </form>
    `;
}

/**
 * Creates the login modal HTML structure
 */
function createLoginModal() {
    // Create modal HTML
    const modalHTML = `
        <div id="login-modal" class="modal-overlay">
            <div class="modal-container">
                <div class="modal-header">
                    <h3 class="modal-title">Log in to Chess</h3>
                    <button class="modal-close">&times;</button>
                </div>
                <div class="modal-body">
                    <!-- Google Sign In Button -->
                    <button class="social-login-btn google-btn" onclick="handleGoogleSignIn()">
                        <img src="https://www.google.com/favicon.ico" alt="Google Icon">
                        Continue with Google
                    </button>

                    <!-- Divider -->
                    <div class="divider">
                        <span class="divider-line"></span>
                        <span class="divider-text">or</span>
                        <span class="divider-line"></span>
                    </div>

                    <div id="login-form-container">
                        <form id="login-form" class="login-form" action="/login" method="POST">
                            <div class="form-group">
                                <label for="modal-username">Email or username</label>
                                <input type="text" id="modal-username" name="username" placeholder="Email or username" required autocomplete="username">
                            </div>
                            <div class="form-group">
                                <label for="modal-password">Password</label>
                                <div class="password-input">
                                    <input type="password" id="modal-password" name="password" placeholder="Password" required autocomplete="current-password">
                                    <button type="button" class="toggle-password" onclick="togglePasswordVisibility()">
                                        <img src="/images/eye.svg" alt="Toggle password visibility">
                                    </button>
                                </div>
                            </div>
                            <div class="remember-me">
                                <input type="checkbox" id="modal-remember" name="remember">
                                <label for="modal-remember">Remember Me</label>
                            </div>
                            <button type="submit" class="login-btn">Log In</button>
                        </form>
                        <div class="modal-switch-msg">
                            <span>Don't have an account?</span>
                            <a href="#" class="modal-switch-link" onclick="switchToSignupForm()">Sign up</a>
                        </div>
                        <a href="/forgot-password" class="forgot-password">Forgot your password?</a>
                    </div>
                </div>
            </div>
        </div>
    `;
    
    // Append modal to body
    document.body.insertAdjacentHTML('beforeend', modalHTML);
}

/**
 * Switches the modal content to show the signup form
 */
function switchToSignupForm() {
    const modalTitle = document.querySelector('.modal-title');
    const loginFormContainer = document.getElementById('login-form-container');
    // Update modal title
    modalTitle.textContent = 'Sign up for Chess';
    // Replace login form with signup form and switch message
    loginFormContainer.innerHTML = `
        <form id="signup-form" class="login-form" action="/signup" method="POST">
            <div class="form-group">
                <label for="modal-signup-username">Username</label>
                <input type="text" id="modal-signup-username" name="username" placeholder="Choose a username" required>
            </div>
            <div class="form-group">
                <label for="modal-signup-email">Email</label>
                <input type="email" id="modal-signup-email" name="email" placeholder="Enter your email" required>
            </div>
            <div class="form-group">
                <label for="modal-signup-password">Password</label>
                <div class="password-input">
                    <input type="password" id="modal-signup-password" name="password" placeholder="Create a password" required>
                    <button type="button" class="toggle-password" onclick="toggleSignupPasswordVisibility()">
                        <img src="/images/eye.svg" alt="Toggle password visibility">
                    </button>
                </div>
            </div>
            <div class="form-group">
                <label for="modal-signup-confirm-password">Confirm Password</label>
                <div class="password-input">
                    <input type="password" id="modal-signup-confirm-password" name="confirmPassword" placeholder="Confirm your password" required>
                    <button type="button" class="toggle-password" onclick="toggleSignupConfirmPasswordVisibility()">
                        <img src="/images/eye.svg" alt="Toggle password visibility">
                    </button>
                </div>
            </div>
            <button type="submit" class="login-btn">Sign Up</button>
        </form>
        <div class="modal-switch-msg">
            <span>Already have an account?</span>
            <a href="#" class="modal-switch-link" onclick="switchToLoginForm()">Log in</a>
        </div>
    `;
    // Setup signup form event listeners
    setupSignupFormEventListeners();
}

/**
 * Switches the modal content back to the login form
 */
function switchToLoginForm() {
    const modalTitle = document.querySelector('.modal-title');
    const loginFormContainer = document.getElementById('login-form-container');
    // Update modal title
    modalTitle.textContent = 'Log in to Chess';
    // Replace signup form with login form and switch message
    loginFormContainer.innerHTML = `
        <form id="login-form" class="login-form" action="/login" method="POST">
            <div class="form-group">
                <label for="modal-username">Email or username</label>
                <input type="text" id="modal-username" name="username" placeholder="Email or username" required autocomplete="username">
            </div>
            <div class="form-group">
                <label for="modal-password">Password</label>
                <div class="password-input">
                    <input type="password" id="modal-password" name="password" placeholder="Password" required autocomplete="current-password">
                    <button type="button" class="toggle-password" onclick="togglePasswordVisibility()">
                        <img src="/images/eye.svg" alt="Toggle password visibility">
                    </button>
                </div>
            </div>
            <div class="remember-me">
                <input type="checkbox" id="modal-remember" name="remember">
                <label for="modal-remember">Remember Me</label>
            </div>
            <button type="submit" class="login-btn">Log In</button>
        </form>
        <div class="modal-switch-msg">
            <span>Don't have an account?</span>
            <a href="#" class="modal-switch-link" onclick="switchToSignupForm()">Sign up</a>
        </div>
        <a href="/forgot-password" class="forgot-password">Forgot your password?</a>
    `;
    // Setup login form event listeners
    setupModalEventListeners();
}

/**
 * Submits the signup form data to the server
 */
async function submitSignupForm(username, email, password) {
    try {
        // Send data to server
        const response = await fetch('/api/users/signup', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify({ username, email, password })
        });
        
        // Check the content type of the response
        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
            // Handle JSON response
            const data = await response.json();
            if (response.ok) {
                console.log('Created user:', data);
                alert('Signup successful!');
                
                // Perform immediate login
                const formData = new FormData();
                formData.append('username', data.credentials.username);
                formData.append('password', data.credentials.password);
                
                const loginResponse = await fetch('/login', {
                    method: 'POST',
                    body: formData,
                    credentials: 'same-origin'
                });
                
                if (loginResponse.ok) {
                    // Just hide the modal without closing it (to preserve pendingAction)
                    const modal = document.getElementById('login-modal');
                    modal.classList.remove('active');
                    document.body.style.overflow = '';
                    // Reload the page
                    window.location.reload();
                } else {
                    console.error('Auto-login failed, redirecting to login page');
                    closeModal();
                    window.location.href = '/index';
                }
            } else {
                // Handle JSON error response
                const errorMessage = data.error || data.message || 'Unknown error occurred';
                console.error('Server error:', data);
                alert('Signup failed: ' + errorMessage);
            }
        } else {
            // Handle non-JSON response
            const errorText = await response.text();
            console.error('Server returned non-JSON response:', errorText);
            if (response.status === 403) {
                alert('Access denied. Please try again later.');
            } else if (response.status === 400) {
                alert('Invalid input. Please check your details and try again.');
            } else {
                alert('An error occurred during signup. Please try again later.');
            }
        }
    } catch (error) {
        console.error('Network error:', error);
        alert('Network error occurred. Please check your connection and try again.');
    }
}

/**
 * Sets up event listeners for the signup form
 */
function setupSignupFormEventListeners() {
    const signupForm = document.getElementById('signup-form');
    
    signupForm.addEventListener('submit', function(event) {
        event.preventDefault();
        
        // Get form data
        const username = document.getElementById('modal-signup-username').value;
        const email = document.getElementById('modal-signup-email').value;
        const password = document.getElementById('modal-signup-password').value;
        const confirmPassword = document.getElementById('modal-signup-confirm-password').value;
        
        // Basic validation
        if (password !== confirmPassword) {
            alert('Passwords do not match');
            return;
        }

        // Validate username length
        if (username.length < 3 || username.length > 50) {
            alert('Username must be between 3 and 50 characters');
            return;
        }

        // Validate email format
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            alert('Please enter a valid email address');
            return;
        }
        
        // Submit form data to server
        submitSignupForm(username, email, password);
    });
}

/**
 * Toggles visibility of the signup password field
 */
function toggleSignupPasswordVisibility() {
    const passwordInput = document.getElementById('modal-signup-password');
    passwordInput.type = passwordInput.type === 'password' ? 'text' : 'password';
}

/**
 * Toggles visibility of the signup confirm password field
 */
function toggleSignupConfirmPasswordVisibility() {
    const passwordInput = document.getElementById('modal-signup-confirm-password');
    passwordInput.type = passwordInput.type === 'password' ? 'text' : 'password';
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
            // Reload the page without clearing pendingAction
            window.location.reload();
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
        
        // Store the game ID in sessionStorage to show popup after redirect
        sessionStorage.setItem('showGamePopup', data.id);
        
        // Redirect to index page instead of game page
        window.location.href = '/index';
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

// Add password visibility toggle function
function togglePasswordVisibility() {
    const passwordInput = document.getElementById('modal-password');
    const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
    passwordInput.setAttribute('type', type);
}

// Add Google Sign In handler
function handleGoogleSignIn() {
    // Redirect to the OAuth2 login endpoint
    window.location.href = '/oauth2/authorization/google';
}

// Export functions for use in other scripts
window.openLoginModal = openLoginModal;
window.showLoginForAction = showLoginForAction;
window.handlePendingAction = handlePendingAction; 