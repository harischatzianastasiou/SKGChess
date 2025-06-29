// This file contains JavaScript for matchmaking functionality

let stompClient = null; // Initialize stompClient to manage WebSocket connection
let reconnectAttempts = 0; // Counter for reconnection attempts
const maxReconnectAttempts = 5; // Maximum number of reconnection attempts

// Function to get current username dynamically
function getCurrentUsername() {
    // Try multiple possible selectors for the username
    const selectors = [
        'span[data-username="true"]', // This is the correct one from the HTML
        'h1 span[sec\\:authentication="name"]',
        '.hero-content h1 span',
        '[data-username]',
        '.username'
    ];
    
    for (const selector of selectors) {
        const element = document.querySelector(selector);
        if (element && element.textContent && element.textContent.trim()) {
            return element.textContent.trim();
        }
    }
    
    // If no username found, log what elements are available
    selectors.forEach(selector => {
        const elements = document.querySelectorAll(selector);
        elements.forEach((el, index) => {
        });
    });
    
    return '';
}

// Function to wait for username to be available
function waitForUsername(maxAttempts = 30, interval = 300) {
    return new Promise((resolve, reject) => {
        let attempts = 0;
        
        const checkUsername = () => {
            attempts++;
            const username = getCurrentUsername();
            
            if (username) {
                resolve(username);
            } else if (attempts >= maxAttempts) {
              
                reject(new Error('Username not available after maximum attempts'));
            } else {
                setTimeout(checkUsername, interval);
            }
        };
        
        checkUsername();
    });
}

// Connect when page loads
// connect();

// Check for pending actions after page load
document.addEventListener('DOMContentLoaded', function() {
    // If user is authenticated, check for pending actions
    if (document.body.classList.contains('authenticated')) {
        // Connect to WebSocket for real-time notifications with a delay to ensure page is loaded
        setTimeout(() => {
            connect();
        }, 1000);
        
        // Only handle pending action if one exists
        const pendingAction = sessionStorage.getItem('pendingAction');
        if (pendingAction) {
            handlePendingAction();
        }
        // Render game boards
        renderGameBoards();
        
        // Check for pending invitations and show notifications
        checkForPendingInvitationsOnLoad();
        
        // Start fallback polling for invitations (in case WebSocket fails)
        startInvitationPolling();
    }
    
    // Check if we need to show a game popup
    const gameId = localStorage.getItem('showGamePopup');
    if (gameId) {
        // Clear the stored game ID
        localStorage.removeItem('showGamePopup');
        // Show the share popup
        showShareGamePopup(gameId);
    } else {
        // If no popup is being shown but there's a gameId in localStorage, delete it
        const pendingGameId = localStorage.getItem('pendingGameId');
        if (pendingGameId) {
            // Delete the pending game
            fetch(`/api/games/${pendingGameId}`, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json'
                }
            }).finally(() => {
                // Clear the pending game ID regardless of success
                localStorage.removeItem('pendingGameId');
            });
        }
    }

    document.querySelectorAll('.clickable-row').forEach(function(row) {
        row.addEventListener('click', function() {
            var gameId = this.getAttribute('data-game-id');
            if (gameId) {
                window.location.href = '/games/' + gameId;
            }
        });
    });
});

// Function to connect to the WebSocket
function connect() {
    
    
    // Check if user is authenticated
    if (!document.body.classList.contains('authenticated')) {
        
        return;
    }
    
    const socket = new SockJS('/chess-websocket'); // Create a new SockJS connection
    stompClient = Stomp.over(socket); // Wrap the socket with Stomp

    stompClient.connect({}, 
        function(frame) {
            
            reconnectAttempts = 0;

            // Fetch user ID and set up subscriptions
            waitForUsername()
                .then(username => {
                    
                    return fetchUserIdByUsername(username);
                })
                .then(userId => {
                    
                    
                    // Subscribe to game creation notifications
                    stompClient.subscribe('/topic/newGame/' + userId, function(message) {
                        
                        try {
                            // Parse the message body as JSON
                            const game = JSON.parse(message.body);
                            
                            // Handle the game creation response
                            handleGameCreated(game);
                        } catch (error) {
                            
                            
                            // Fall back to the old message handling
                            handleGameMessage(message.body);
                        }
                    });

                    // Subscribe to invitation notifications
                    stompClient.subscribe('/topic/user/' + userId, function(message) {
                        
                        try {
                            const notification = JSON.parse(message.body);
                            handleInvitationNotification(notification);
                        } catch (error) {
                            
                        }
                    });

                    // Subscribe to errors
                    stompClient.subscribe('/user/queue/errors', function(message) {
                        
                        alert('Error: ' + message.body);
                    });
                    
                    
                })
                .catch(error => {
                    
                    
                    
                    
                    // Show user-friendly message about limited functionality
                    if (error.message.includes('not found in database')) {
                        
                    } else if (error.message.includes('not authenticated')) {
                        
                    } else if (error.message.includes('Username not available')) {
                        
                        
                    } else {
                        
                    }
                    
                    // Don't disconnect the WebSocket, just log the error
                    // The user can still use other features, just won't get real-time notifications
                    // Polling will still work as a fallback
                });
        },
        function(error) {
            
            handleDisconnect();
        }
    );
}

// Track if share popup is open and game messages
let isSharePopupOpen = false;
let gameMessages = [];

// Add visibility change handler
document.addEventListener('visibilitychange', function() {
    if (document.visibilityState === 'hidden' && isSharePopupOpen) {
        const gameId = document.querySelector('.share-game-popup')?.getAttribute('data-game-id');
        if (gameId) {
            // Store the game ID to be deleted on next page load
            localStorage.setItem('pendingGameId', gameId);
        }
    }
});

// Function to show the share game popup
function showShareGamePopup(gameId) {
    // Store the game ID in localStorage
    localStorage.setItem('pendingGameId', gameId);
    
    // Create popup container
    const popup = document.createElement('div');
    popup.className = 'share-game-popup';
    popup.setAttribute('data-game-id', gameId);  // Add game ID as data attribute
    
    // Create popup content with modern design
    popup.innerHTML = `
        <div class="popup-header">
            <img src="/images/skgchess.svg" alt="SKGChess Logo" class="popup-header-logo">
            <button class="close-btn" onclick="confirmClosePopup('${gameId}')">
                <i class="fas fa-times"></i>
            </button>
        </div>
        <div class="popup-body">
            <div class="game-id-container">
                <p>Share room ID with your opponent :</p>
                <div class="game-id-box">
                    <input type="text" value="${gameId}" readonly>
                    <button class="copy-btn" onclick="copyGameId('${gameId}')">
                        <i class="fas fa-copy"></i> Copy
                    </button>
                </div>
            </div>
            <div class="info-box">
                <i class="fas fa-info-circle"></i>
                <p>The game will start automatically when your opponent joins.</p>
                <div class="loading-container">
                    <div class="loading-spinner"></div>
                </div>
            </div>
        </div>
    `;

    // Add overlay with blur effect
    const overlay = document.createElement('div');
    overlay.className = 'share-game-overlay';
    overlay.onclick = () => confirmClosePopup(gameId); // Add click handler for overlay

    // Add to document
    document.body.appendChild(overlay);
    document.body.appendChild(popup);

    // Show popup with animation
    setTimeout(() => {
        popup.classList.add('show');
        overlay.classList.add('show');
        isSharePopupOpen = true;
    }, 10);

    // Subscribe to WebSocket for game start
    subscribeToGameStart(gameId);
}

// Function to show confirmation dialog
function confirmClosePopup(gameId) {
    const confirmDialog = document.createElement('div');
    confirmDialog.className = 'confirm-dialog';
    confirmDialog.innerHTML = `
        <div class="confirm-content">
            <h4>Cancel Game Creation?</h4>
            <div class="confirm-buttons">
                <button class="btn-no" onclick="this.closest('.confirm-dialog').remove()">Keep Waiting</button>
                <button class="btn-yes" onclick="closeSharePopup('${gameId}')">Cancel Game</button>
            </div>
        </div>
    `;
    document.body.appendChild(confirmDialog);

    // Add show class after a small delay to trigger animation
    setTimeout(() => {
        confirmDialog.classList.add('show');
    }, 10);

    // Close on escape key
    const handleEscape = (e) => {
        if (e.key === 'Escape') {
            confirmDialog.classList.remove('show');
            setTimeout(() => confirmDialog.remove(), 300);
            document.removeEventListener('keydown', handleEscape);
        }
    };
    document.addEventListener('keydown', handleEscape);

    // Close on clicking outside
    confirmDialog.addEventListener('click', (e) => {
        if (e.target === confirmDialog) {
            confirmDialog.classList.remove('show');
            setTimeout(() => confirmDialog.remove(), 300);
        }
    });
}

// Function to close the share game popup
function closeSharePopup(gameId) {
    const popup = document.querySelector('.share-game-popup');
    const overlay = document.querySelector('.share-game-overlay');
    const confirmDialog = document.querySelector('.confirm-dialog');
    
    // First remove the confirmation dialog with animation
    if (confirmDialog) {
        confirmDialog.classList.remove('show');
        setTimeout(() => confirmDialog.remove(), 150);
    }
    
    if (popup) {
        // Call the delete endpoint
        fetch(`/api/games/${gameId}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            }
        })
        .then(response => {
            if (!response.ok) {
                
            }
        })
        .catch(error => {
            
        })
        .finally(() => {
            // Remove popup and overlay with animations
            popup.classList.remove('show');
            overlay.classList.remove('show');
            isSharePopupOpen = false;
            
            // Remove elements after animation
            setTimeout(() => {
                popup.remove();
                overlay.remove();
            }, 150);
        });
    }
}

// Function to subscribe to game start
function subscribeToGameStart(gameId) {
    const socket = new SockJS('/chess-websocket');
    const stompClient = Stomp.over(socket);
    
    // Enable debug logging for STOMP
    stompClient.debug = function(str) {
        
    };
    
    stompClient.connect({}, 
        (frame) => {
            
            
            stompClient.subscribe('/topic/game/' + gameId, (message) => {
                try {
                    const data = JSON.parse(message.body);
                    
                    if (data.type === 'GAME_STARTED') {
                        
                        if (isSharePopupOpen) {
                            // If popup is still open, redirect to game page
                            
                            window.location.href = `/games/${gameId}`;
                        }
                    }
                } catch (error) {
                    
                }
            });
        },
        (error) => {
            
        }
    );
}

// Remove the messages icon from header when document is loaded
document.addEventListener('DOMContentLoaded', function() {
    const headerRight = document.querySelector('.header-right');
    if (headerRight) {
        // Remove any existing messages container
        const messagesContainer = headerRight.querySelector('.header-messages');
        if (messagesContainer) {
            messagesContainer.remove();
        }
    }
});

/**
 * Copies the game ID to clipboard
 * @param {string} gameId - The ID to copy
 */
function copyGameId(gameId) {
    navigator.clipboard.writeText(gameId).then(() => {
        // Show success message
        const button = document.querySelector('.copy-btn');
        const originalText = button.innerHTML;
        button.innerHTML = '<i class="fas fa-check"></i> Copied!';
        button.classList.add('success');
        
        // Reset button after 2 seconds
        setTimeout(() => {
            button.innerHTML = originalText;
            button.classList.remove('success');
        }, 2000);
    }).catch(err => {
        
        alert('Failed to copy game ID. Please try again.');
    });
}

function newGame() {
    // Check if user is logged in
    const usernameElement = document.querySelector('span[data-username="true"]');
    if (!usernameElement) {
        // User is not logged in, show login modal
        showLoginForAction('newGame');
        return;
    }

    // Show the game creation popup
    showGameCreationPopup();
}

// Function to show game creation popup
function showGameCreationPopup() {
    // Create popup HTML with new time control dropdown
    const popupHTML = `
        <div id="gameCreationPopup" class="game-creation-popup">
            <div class="popup-content">
                <div class="popup-header">
                    <h3>Create New Game</h3>
                    <button class="close-btn" onclick="closeGameCreationPopup()">
                        <i class="fas fa-times"></i>
                    </button>
                </div>
                <div class="popup-body">
                    <div class="form-group">
                        <label for="opponentSearch">Search for opponent:</label>
                        <div class="search-container">
                            <input type="text" id="opponentSearch" placeholder="Enter username to search..." autocomplete="off">
                            <div id="searchResults" class="search-results"></div>
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="timeControl">Time Control:</label>
                        <div class="custom-dropdown" id="timeControlDropdown">
                            <div class="dropdown-trigger" id="timeControlTrigger">
                                <span class="dropdown-text">Select time control</span>
                                <i class="fas fa-chevron-down"></i>
                            </div>
                            <div class="dropdown-menu" id="timeControlMenu">
                                <div class="dropdown-group">
                                    <div class="dropdown-group-label">Bullet</div>
                                    <div class="dropdown-option" data-value="1|0">1 min</div>
                                    <div class="dropdown-option" data-value="1|1">1 | 1</div>
                                    <div class="dropdown-option" data-value="2|1">2 | 1</div>
                                    <div class="dropdown-option" data-value="0.5|0">30 sec</div>
                                    <div class="dropdown-option" data-value="0.33|1">20 sec | 1</div>
                                </div>
                                <div class="dropdown-group">
                                    <div class="dropdown-group-label">Blitz</div>
                                    <div class="dropdown-option" data-value="3|0">3 min</div>
                                    <div class="dropdown-option" data-value="3|2">3 | 2</div>
                                    <div class="dropdown-option" data-value="5|0">5 min</div>
                                    <div class="dropdown-option" data-value="5|5">5 | 5</div>
                                    <div class="dropdown-option" data-value="5|2">5 | 2</div>
                                </div>
                                <div class="dropdown-group">
                                    <div class="dropdown-group-label">Rapid</div>
                                    <div class="dropdown-option" data-value="10|0">10 min</div>
                                    <div class="dropdown-option" data-value="10|5">10 | 5</div>
                                    <div class="dropdown-option" data-value="15|10">15 | 10</div>
                                    <div class="dropdown-option" data-value="20|0">20 min</div>
                                    <div class="dropdown-option" data-value="30|0">30 min</div>
                                    <div class="dropdown-option" data-value="60|0">60 min</div>
                                </div>
                            </div>
                            <input type="hidden" id="timeControl" required>
                        </div>
                    </div>
                    <div class="form-group">
                        <label>Your Color:</label>
                        <div class="color-selection">
                            <label class="color-option">
                                <input type="radio" name="playerColor" value="white" checked>
                                <span class="color-box white-piece">♔</span>
                                <span>White</span>
                            </label>
                            <label class="color-option">
                                <input type="radio" name="playerColor" value="black">
                                <span class="color-box black-piece">♚</span>
                                <span>Black</span>
                            </label>
                            <label class="color-option">
                                <input type="radio" name="playerColor" value="random">
                                <span class="color-box random-piece">?</span>
                                <span>Random</span>
                            </label>
                        </div>
                    </div>
                    <div class="create-button-container">
                        <button class="btn-create" onclick="createInvitation()" id="createInvitationBtn" disabled>Send Invitation</button>
                    </div>
                </div>
            </div>
        </div>
    `;
    
    // Add popup to body
    document.body.insertAdjacentHTML('beforeend', popupHTML);
    
    // Show the popup with animation
    setTimeout(() => {
        const popup = document.getElementById('gameCreationPopup');
        if (popup) {
            popup.classList.add('show');
            
            // On mobile, scroll to bottom to ensure create button is visible
            if (window.innerWidth <= 768) {
                setTimeout(() => {
                    const popupBody = popup.querySelector('.popup-body');
                    if (popupBody) {
                        popupBody.scrollTop = popupBody.scrollHeight;
                    }
                }, 200);
            }
        }
    }, 10);
    
    // Add escape key listener
    const handleEscape = (e) => {
        if (e.key === 'Escape') {
            closeGameCreationPopup();
            document.removeEventListener('keydown', handleEscape);
        }
    };
    document.addEventListener('keydown', handleEscape);
    
    // Add click outside to close
    const popup = document.getElementById('gameCreationPopup');
    const handleOutsideClick = (e) => {
        if (e.target === popup) {
            closeGameCreationPopup();
            popup.removeEventListener('click', handleOutsideClick);
        }
    };
    popup.addEventListener('click', handleOutsideClick);
    
    // Focus on opponent search input
    setTimeout(() => {
        const opponentSearch = document.getElementById('opponentSearch');
        if (opponentSearch) {
            opponentSearch.focus();
        }
    }, 100);
    
    // Add search functionality
    setupOpponentSearch();
    
    // Setup custom dropdown functionality
    setTimeout(() => {
        setupCustomDropdown();
    }, 150);
}

// Function to close game creation popup
function closeGameCreationPopup() {
    const popup = document.getElementById('gameCreationPopup');
    if (popup) {
        // Remove show class for smooth animation
        popup.classList.remove('show');
        
        // Wait for animation to complete before removing
        setTimeout(() => {
            popup.remove();
        }, 300);
    }
}

// Function to create game with selected options
function createGameWithOptions() {
    // Get form values
    const timeControlValue = document.getElementById('timeControl').value; // e.g., "3|2"
    const playerColor = document.querySelector('input[name="playerColor"]:checked').value;
    // Validate form
    if (!timeControlValue) {
        showErrorPopup('Please select a time control');
        return;
    }
    // Parse minutes and increment
    let [minutes, increment] = timeControlValue.split('|');
    minutes = parseFloat(minutes); // Handles "0.5" for 30 sec, etc.
    increment = parseInt(increment); // Increment in seconds
    // Get username
    const usernameElement = document.querySelector('span[data-username="true"]');
    if (!usernameElement) {
        showErrorPopup('User not found. Please try logging in again.');
        return;
    }
    const username = usernameElement.textContent;
    // Create the request body according to CreateGameRequestDTO
    const requestBody = {
        username: username,
        timeControlMinutes: minutes, // Pass minutes
        incrementSeconds: increment, // Pass increment
        playerColor: playerColor // Pass the selected color
    };
    // Close popup first
    closeGameCreationPopup();
    // Call the create game endpoint with the request body
    fetch('/api/games', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestBody)
    })
    .then(response => {
        if (!response.ok) {
            return response.json().then(errorData => {
                showErrorPopup(errorData.message);
                throw new Error(errorData.message);
            });
        }
        return response.json();
    })
    .then(data => {
        if (!data) return; // Return if we showed an error popup
        if (!data || !data.id) {
            throw new Error('Game ID not found in response');
        }
        // Show the share popup instead of redirecting immediately
        showShareGamePopup(data.id);
    })
    .catch(error => {
        // Don't show another error popup here since we already showed one in the response handling
    });
}

function joinGame(gameId) {
    const usernameElement = document.querySelector('span[data-username="true"]');
    if (!usernameElement) {
        
        alert('Error: Could not find username. Please try logging in again.');
        return;
    }
    const username = usernameElement.textContent;
    
     // Create the request body according to CreateGameRequestDTO
     const requestBody = {
        username: username,
        gameId : gameId
    };
    
    
    // Call the join game endpoint
    fetch('/api/games/join', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestBody)
    })
    .then(response => {
        
        if (!response.ok) {
            return response.json().then(errorData => {
                showErrorPopup(errorData.message);
                throw new Error(errorData.message);
            });
        }
        return response.json();
    })
    .then(data => {
        if (!data) return; // Return if we showed an error popup
        if (!data.id) {
            throw new Error('Game ID not found in response');
        }
        
        
        // Redirect to the game page
        window.location.href = `/games/${data.id}`;
    })
    .catch(error => {
        
        // Don't show another error popup here since we already showed one in the response handling
    });
}

// Function to fetch user ID by username
function fetchUserIdByUsername(username) {
    
    
    // Check if username is valid
    if (!username || username.trim() === '') {
        throw new Error('Username is empty or invalid');
    }
    
    return fetch(`/api/users/${username}`)
        .then(response => {
            
            
            if (response.status === 404) {
                throw new Error('User not found in database');
            } else if (response.status === 401) {
                throw new Error('User not authenticated');
            } else if (response.status === 500) {
                throw new Error('Server error occurred');
            } else if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
            
            return response.json();
        })
        .then(user => {
            
            if (!user || !user.id) {
                throw new Error('User ID not found in response');
            }
            
            return user.id;
        })
        .catch(error => {
            
            
            
            throw error;
        });
}

// Function to handle incoming game messages
function handleGameMessage(message) {
    
    
    // If message is already a string, use it directly, otherwise stringify it
    const messageStr = typeof message === 'string' ? message : JSON.stringify(message);
    
    
    
    try {
        // Try to parse as JSON first
        const jsonData = JSON.parse(messageStr);
        
        
        if (jsonData && jsonData.id) {
            
            alert('Game created successfully! Redirecting to game ' + jsonData.id);
            window.location.href = '/api/games/' + jsonData.id;
            return;
        }
    } catch (e) {
        
    }
    
    // If not valid JSON or doesn't have expected format, process as text
    if (messageStr.includes('Game created:')) {
        const gameId = messageStr.split(':')[2].trim();  // Get the ID and trim whitespace
        
        alert('Game created successfully! Redirecting to game ' + gameId);
        window.location.href = '/game/' + gameId;
    } else {
        alert(messageStr);
    }
}

// Function to handle game creation response
function handleGameCreated(game) {
    
    
    // Check if game object is valid and has an ID
    if (game && game.id) {
        
        alert('Game created successfully! Redirecting to game ' + game.id);
        window.location.href = '/api/games/' + game.id;
    } else {
        // Handle error case
        
        alert('Error creating game. Please try again.');
    }
}


// Cleanup on page unload
window.onbeforeunload = function() {
    if (stompClient !== null) {
        stompClient.disconnect(); // Disconnect on page unload
    }
};

// Helper function to create error element if it doesn't exist
function createErrorElement() {
    const errorDiv = document.createElement('div');
    errorDiv.id = 'error-message';
    errorDiv.className = 'alert alert-danger';
    errorDiv.style.marginTop = '20px';
    errorDiv.style.padding = '10px';
    errorDiv.style.borderRadius = '5px';
    errorDiv.style.backgroundColor = '#f8d7da';
    errorDiv.style.color = '#721c24';
    errorDiv.style.border = '1px solid #f5c6cb';
    
    // Insert after the form
    const form = document.querySelector('form');
    if (form) {
        form.parentNode.insertBefore(errorDiv, form.nextSibling);
    } else {
        document.body.appendChild(errorDiv);
    }
    
    return errorDiv;
} 

// Performance optimized scroll and intersection observer handling
document.addEventListener('DOMContentLoaded', function() {
    // Initialize variables for scroll optimization
    let scrollTimeout;
    let isScrolling = false;
    let currentSection = 0;
    let lastScrollTime = 0; // Track the last scroll time
    const scrollDebounceTime = 100; // Minimum time between scroll events in milliseconds
    const body = document.body;
    const sections = document.querySelectorAll('section');
    
    // Intersection Observer for progressive loading and animations
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('visible');
                // Update current section index
                currentSection = Array.from(sections).indexOf(entry.target);
                // Lazy load images in the visible section
                entry.target.querySelectorAll('img[data-src]').forEach(img => {
                    img.src = img.dataset.src;
                    img.removeAttribute('data-src');
                });
            }
        });
    }, {
        threshold: 0.3, // Reduced threshold for better section detection
        rootMargin: '-80px 0px 0px 0px' // Account for fixed header
    });

    // Observe all sections
    sections.forEach(section => {
        observer.observe(section);
    });

    // Smooth scroll to section function
    function scrollToSection(index) {
        // Check if we're already scrolling or if we're within the debounce time
        const currentTime = Date.now();
        if (isScrolling || index < 0 || index >= sections.length || currentTime - lastScrollTime < scrollDebounceTime) return;
        
        isScrolling = true;
        lastScrollTime = currentTime;
        
        // Calculate the target scroll position
        const headerHeight = 80; // Height of fixed header
        const targetSection = sections[index];
        const targetPosition = targetSection.offsetTop - headerHeight;
        
        // Smooth scroll to the target position
        window.scrollTo({
            top: targetPosition,
            behavior: 'smooth'
        });
        
        currentSection = index;
        
        // Add a small delay before allowing the next scroll
        setTimeout(() => {
            isScrolling = false;
        }, 800); // Match this with your CSS transition duration
    }

    // Function to check if we're on mobile
    function isMobileScreen() {
        return window.innerWidth <= 1000;
    }

    // Function to handle mobile detection and cleanup
    function handleMobileDetection() {
        if (isMobileScreen()) {
            // Remove all section-based scroll event listeners
            window.removeEventListener('wheel', wheelHandler);
            window.removeEventListener('touchstart', touchStartHandler);
            window.removeEventListener('touchend', touchEndHandler);
            window.removeEventListener('keydown', keydownHandler);
            
            // Enable natural scrolling
            document.body.style.overflow = 'auto';
            document.documentElement.style.overflow = 'auto';
            
            // Remove any scroll prevention
            document.body.style.height = '';
            document.documentElement.style.height = '';
            
            // Add passive scroll listener to prevent section-based behavior
            window.addEventListener('scroll', function preventSectionScroll(e) {
                e.stopPropagation();
            }, { passive: true });
        } else {
            // Re-add event listeners for desktop
            window.addEventListener('wheel', wheelHandler, { passive: false });
            window.addEventListener('touchstart', touchStartHandler, { passive: true });
            window.addEventListener('touchend', touchEndHandler, { passive: true });
            window.addEventListener('keydown', keydownHandler);
        }
    }

    // Handle wheel events for smooth scrolling
    function wheelHandler(e) {
        // Exit early if on mobile
        if (isMobileScreen()) return;

        // Check if newspaper overlay is active
        const newspaperOverlay = document.getElementById('newspaperOverlay');
        if (newspaperOverlay && newspaperOverlay.style.display === 'block') {
            // Completely disable custom scroll handling when newspaper is open
            return;
        }

        // Find the closest scrollable container
        const scrollableContainer = e.target.closest('.all-games-section');
        
        if (scrollableContainer) {
            // Get the total scrollable height
            const totalHeight = scrollableContainer.scrollHeight;
            // Get the visible height
            const visibleHeight = scrollableContainer.clientHeight;
            // Get the current scroll position
            const currentScroll = scrollableContainer.scrollTop;
            
            // Check if we're at the top or bottom of the scrollable content
            const isAtTop = currentScroll === 0;
            const isAtBottom = currentScroll + visibleHeight >= totalHeight;
            
            // If scrolling up at the top or down at the bottom, allow section navigation
            if ((e.deltaY < 0 && isAtTop) || (e.deltaY > 0 && isAtBottom)) {
                // Allow section navigation
                if (isScrolling) return;
                
                e.preventDefault();
                setTimeout(() => {
                    if (e.deltaY > 0) {
                        scrollToSection(currentSection + 1);
                    } else {
                        scrollToSection(currentSection - 1);
                    }
                }, 50);
            } else {
                // Otherwise, allow natural scrolling within the container
                e.stopPropagation();
            }
            return;
        }
        
        // Default section navigation behavior for non-scrollable areas
        if (isScrolling) return;
        
        e.preventDefault();
        setTimeout(() => {
            if (e.deltaY > 0) {
                scrollToSection(currentSection + 1);
            } else if (e.deltaY < 0) {
                scrollToSection(currentSection - 1);
            }
        }, 50);
    }

    // Handle touch events for mobile
    let touchStartY = 0;
    
    function touchStartHandler(e) {
        // Exit early if on mobile
        if (isMobileScreen()) return;
        
        // Check if the event originated from a scrollable container
        const scrollableParent = e.target.closest('.scrollable-content, .scrollable-features');
        if (scrollableParent) {
            // Allow natural touch handling within scrollable containers
            return;
        }

        touchStartY = e.touches[0].clientY;
    }

    function touchEndHandler(e) {
        // Exit early if on mobile
        if (isMobileScreen()) return;
        
        // Check if the event originated from a scrollable container
        const scrollableParent = e.target.closest('.scrollable-content, .scrollable-features');
        if (scrollableParent) {
            // Allow natural touch scrolling within scrollable containers
            return;
        }

        if (isScrolling) return;
        
        const touchEndY = e.changedTouches[0].clientY;
        const diff = touchStartY - touchEndY;
        
        if (Math.abs(diff) > 50) { // Minimum swipe distance
            if (diff > 0) {
                scrollToSection(currentSection + 1);
            } else {
                scrollToSection(currentSection - 1);
            }
        }
    }

    // Handle keyboard navigation
    function keydownHandler(e) {
        // Exit early if on mobile
        if (isMobileScreen()) return;
        
        if (isScrolling) return;
        
        if (e.key === 'ArrowDown' || e.key === 'PageDown') {
            scrollToSection(currentSection + 1);
        } else if (e.key === 'ArrowUp' || e.key === 'PageUp') {
            scrollToSection(currentSection - 1);
        }
    }

    // Call handleMobileDetection on load and resize
    handleMobileDetection();
    window.addEventListener('resize', handleMobileDetection);

    // Remove all gesture event listeners
    window.removeEventListener('gesturestart', (e) => {
        e.preventDefault();
    }, { passive: false });

    window.removeEventListener('gesturechange', (e) => {
        e.preventDefault();
    }, { passive: false });

    window.removeEventListener('gestureend', (e) => {
        e.preventDefault();
    }, { passive: false });

    // Smooth scroll implementation
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function(e) {
            e.preventDefault();
            const targetId = this.getAttribute('href');
            if (targetId === '#') return;
            
            const targetElement = document.querySelector(targetId);
            if (targetElement) {
                const headerHeight = 80; // Height of fixed header
                const targetPosition = targetElement.offsetTop - headerHeight;
                
                window.scrollTo({
                    top: targetPosition,
                    behavior: 'smooth'
                });
            }
        });
    });
});

// Game controls handling
function handleGameControls() {
    const gameIdInput = document.getElementById('gameId');
    if (!gameIdInput) return;

    // Add input validation
    gameIdInput.addEventListener('input', (e) => {
        const value = e.target.value;
        // Add validation logic here if needed
    });

    // Add keyboard support for game controls
    gameIdInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            joinGame(gameIdInput.value);
        }
    });
}

// Initialize game controls
document.addEventListener('DOMContentLoaded', handleGameControls);

// Accessibility enhancements
function enhanceAccessibility() {
    // Add keyboard navigation for interactive elements
    const interactiveElements = document.querySelectorAll('button, a, input');
    
    interactiveElements.forEach(element => {
        element.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                element.click();
            }
        });
    });

    // Add ARIA labels where missing
    document.querySelectorAll('.action-card').forEach((card, index) => {
        if (!card.getAttribute('aria-label')) {
            const headingText = card.querySelector('h2')?.textContent || `Action ${index + 1}`;
            card.setAttribute('aria-label', headingText);
        }
    });
}

// Initialize accessibility enhancements
document.addEventListener('DOMContentLoaded', enhanceAccessibility);

// Performance monitoring (optional, remove in production)
function monitorPerformance() {
    if (window.performance && window.performance.mark) {
        window.performance.mark('app-loaded');
        
        // Report performance metrics
        setTimeout(() => {
            const navigationTiming = performance.getEntriesByType('navigation')[0];
            const paintTiming = performance.getEntriesByType('paint');
            
            
            
            
        }, 0);
    }
}

// Initialize performance monitoring
document.addEventListener('DOMContentLoaded', monitorPerformance);

/**
 * Render chess board previews for games
 */
function renderGameBoards() {
    // Get all board preview containers
    const boardPreviews = document.querySelectorAll('.chess-board-preview');
    
    if (boardPreviews.length === 0) {
        return; // No games to render
    }
    
    // For each board preview, create a chess board
    boardPreviews.forEach(boardPreview => {
        // Extract game ID from the element ID
        const gameId = boardPreview.id.replace('board-preview-', '');
        
        // Create a new chess board
        const board = document.createElement('div');
        board.className = 'board-preview';
        boardPreview.appendChild(board);
        
        // Fetch the game data to get the current position
        fetch(`/api/games/${gameId}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to fetch game data');
                }
                return response.json();
            })
            .then(gameData => {
                // Get the board data
                const boardDTO = JSON.parse(gameData.board);
                
                // Render the board with game data
                renderChessBoard(board, boardDTO, gameData);
            })
            .catch(error => {
                
                // Render an empty board if data fetch fails
                renderChessBoard(board, { tiles: [] }, null);
            });
    });
}

/**
 * Render a chess board with the current position
 * @param {HTMLElement} container - The container element
 * @param {Object} boardDTO - The board data transfer object containing tile information
 */
function renderChessBoard(container, boardDTO, gameData) {
    // Clear the container
    container.innerHTML = '';
    
    // Define piece images
    const pieceImages = {
        'WHITE_PAWN': '/images/pawnN.png',
        'WHITE_KNIGHT': '/images/knightN.png',
        'WHITE_BISHOP': '/images/bishopN.png',
        'WHITE_ROOK': '/images/rookN.png',
        'WHITE_QUEEN': '/images/queenN.png',
        'WHITE_KING': '/images/kingN.png',
        'BLACK_PAWN': '/images/pawnN1.png',
        'BLACK_KNIGHT': '/images/knightN1.png',
        'BLACK_BISHOP': '/images/bishopN1.png',
        'BLACK_ROOK': '/images/rookN1.png',
        'BLACK_QUEEN': '/images/queenN1.png',
        'BLACK_KING': '/images/kingN1.png'
    };
    
    // Create the board grid
    const board = document.createElement('div');
    board.className = 'board-grid';
    
    // Get the current user's username
    const usernameElement = document.querySelector('span[data-username="true"]');
    const currentUsername = usernameElement ? usernameElement.textContent : '';
    
    // Add black-perspective class if current user is the black player
    if (gameData && gameData.blackPlayerUsername === currentUsername) {
        board.classList.add('black-perspective');
    }
    
    // Create squares
    for (let i = 0; i < 64; i++) {
        const square = document.createElement('div');
        const isLight = ((Math.floor(i / 8) + i % 8) % 2) === 0;
        square.className = `square ${isLight ? 'light' : 'dark'}`;
        
        // Get the piece at this square from boardDTO
        const tileData = boardDTO.tiles.find(t => t.tileCoordinate === i);
        if (tileData && tileData.tileOccupied && tileData.piece) {
            // Create piece element
            const pieceElement = document.createElement('div');
            pieceElement.className = 'piece';
            const pieceKey = `${tileData.piece.pieceAlliance}_${tileData.piece.pieceSymbol}`;
            pieceElement.style.backgroundImage = `url('${pieceImages[pieceKey]}')`;
            
            // Add piece to square
            square.appendChild(pieceElement);
        }
        
        // Add square to board
        board.appendChild(square);
    }
    
    // Add board to container
    container.appendChild(board);
}

// Function to show the join game dialog
function showJoinGameDialog() {
    // Check if user is logged in
    const usernameElement = document.querySelector('span[data-username="true"]');
    if (!usernameElement) {
        // User is not logged in, show login modal
        showLoginForAction('joinGame');
        return;
    }
    
    const username = usernameElement.textContent;
    
    // Show the join game dialog and overlay
    const dialog = document.getElementById('joinGameDialog');
    const overlay = document.getElementById('joinGameOverlay');
    
    if (dialog && overlay) {
        dialog.classList.add('active');
        overlay.classList.add('active');
        
        // Load pending invitations
        loadPendingInvitations(username);
        
        // Prevent body scrolling
        document.body.style.overflow = 'hidden';
    } else {
        
    }
}

// Function to load pending invitations
function loadPendingInvitations(username) {
    const invitationsContainer = document.getElementById('invitationsContainer');
    if (!invitationsContainer) return;
    
    // Show loading state
    invitationsContainer.innerHTML = '<div class="loading">Loading invitations...</div>';
    
    fetch(`/api/invitations/pending/${username}`)
        .then(response => response.json())
        .then(invitations => {
            if (invitations.length === 0) {
                invitationsContainer.innerHTML = '<div class="no-invitations">No pending invitations</div>';
            } else {
                invitationsContainer.innerHTML = invitations.map(invitation => `
                    <div class="invitation-item" data-invitation-id="${invitation.id}">
                        <div class="invitation-header">
                            <span class="inviter-name">${invitation.inviterUsername}</span>
                            <span class="invitation-time">${invitation.timeControlMinutes} min</span>
                        </div>
                        <div class="invitation-details">
                            <span class="invitation-text">invited you to a game</span>
                            <span class="invitation-color">(You will play as ${invitation.playerColor === 'white' ? 'black' : invitation.playerColor === 'black' ? 'white' : 'random'})</span>
                        </div>
                        <div class="invitation-actions">
                            <button class="btn-accept" onclick="respondToInvitation('${invitation.id}', 'accept')">
                                <i class="fas fa-check"></i> Accept
                            </button>
                            <button class="btn-decline" onclick="respondToInvitation('${invitation.id}', 'decline')">
                                <i class="fas fa-times"></i> Decline
                            </button>
                        </div>
                    </div>
                `).join('');
            }
        })
        .catch(error => {
            
            invitationsContainer.innerHTML = '<div class="error">Error loading invitations</div>';
        });
}

// Function to respond to an invitation
function respondToInvitation(invitationId, action) {
    // Get the username from the page
    const usernameElement = document.querySelector('span[data-username="true"]');
    if (!usernameElement) {
        // Show error if username not found
        showErrorPopup('User not found. Please try logging in again.');
        return;
    }
    
    const username = usernameElement.textContent;
    
    // Prepare the request body
    const requestBody = {
        username: username,
        invitationId: invitationId,
        action: action
    };
    
    // Immediately remove the invitation from the UI to prevent double-clicking
    if (action === 'accept') {
        const invitationElement = document.querySelector(`[data-invitation-id="${invitationId}"]`);
        if (invitationElement) {
            // Disable the buttons to prevent further interaction
            const buttons = invitationElement.querySelectorAll('button');
            buttons.forEach(button => {
                button.disabled = true;
                button.style.opacity = '0.5';
                button.style.cursor = 'not-allowed';
            });
            
            // Add a visual indicator that the invitation is being processed
            invitationElement.style.opacity = '0.7';
            invitationElement.style.pointerEvents = 'none';
        }
    }
    
    // Send the request to the server
    fetch('/api/invitations/respond', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestBody)
    })
    .then(response => {
        // Handle error responses
        if (!response.ok) {
            return response.json().then(errorData => {
                // Re-enable the buttons if there was an error
                const invitationElement = document.querySelector(`[data-invitation-id="${invitationId}"]`);
                if (invitationElement) {
                    const buttons = invitationElement.querySelectorAll('button');
                    buttons.forEach(button => {
                        button.disabled = false;
                        button.style.opacity = '1';
                        button.style.cursor = 'pointer';
                    });
                    invitationElement.style.opacity = '1';
                    invitationElement.style.pointerEvents = 'auto';
                }
                
                showErrorPopup(errorData.message);
                throw new Error(errorData.message);
            });
        }
        return response.json();
    })
    .then(data => {
        // Handle successful response
        if (action === 'accept') {
            showSuccessPopup('Invitation accepted! Redirecting to game...');
            
            // Remove the invitation from the UI immediately
            const invitationElement = document.querySelector(`[data-invitation-id="${invitationId}"]`);
            if (invitationElement) {
                invitationElement.remove();
            }
            
            // Check if there are no more invitations and show appropriate message
            const invitationsContainer = document.getElementById('invitationsContainer');
            if (invitationsContainer && invitationsContainer.children.length === 0) {
                invitationsContainer.innerHTML = '<div class="no-invitations">No pending invitations</div>';
            }
            
            // Redirect to game if gameId is provided (both users should be redirected)
            if (data.gameId) {
                // If this is the inviter, delete the invitation after 3 seconds
                if (username === data.inviterUsername && invitationId) {
                    setTimeout(() => {
                        deleteInvitation(invitationId);
                    }, 3000); // 3 second delay before deletion
                }
                
                setTimeout(() => {
                    window.location.href = `/games/${data.gameId}`;
                }, 2000);
            }
        } else {
            showSuccessPopup('Invitation declined');
            // Remove the declined invitation from the UI immediately
            const invitationElement = document.querySelector(`[data-invitation-id="${invitationId}"]`);
            if (invitationElement) {
                invitationElement.remove();
            }
            
            // Check if there are no more invitations and show appropriate message
            const invitationsContainer = document.getElementById('invitationsContainer');
            if (invitationsContainer && invitationsContainer.children.length === 0) {
                invitationsContainer.innerHTML = '<div class="no-invitations">No pending invitations</div>';
            }
        }
    })
    .catch(error => {
        // Error handling is already done in the response.ok check above
    });
}

// Function to close the join game dialog
function closeJoinGameDialog() {
    // Hide the join game dialog and overlay
    const dialog = document.getElementById('joinGameDialog');
    const overlay = document.getElementById('joinGameOverlay');
    
    if (dialog && overlay) {
        dialog.classList.remove('active');
        overlay.classList.remove('active');
        
        // Restore body scrolling
        document.body.style.overflow = '';
    }
}

// Add event listener for escape key
document.addEventListener('keydown', function(event) {
    if (event.key === 'Escape' && isSharePopupOpen) {
        const gameId = document.querySelector('.share-game-popup')?.getAttribute('data-game-id');
        if (gameId) {
            confirmClosePopup(gameId);
        }
    }
});

// Function to show error popup
function showErrorPopup(message) {
    // Remove any existing error popup
    const existingPopup = document.querySelector('.error-popup');
    if (existingPopup) {
        existingPopup.remove();
    }
    
    // Create error popup
    const popup = document.createElement('div');
    popup.className = 'error-popup';
    popup.innerHTML = `
        <div class="error-content">
            <i class="fas fa-exclamation-triangle"></i>
            <p>${message}</p>
            <button onclick="this.parentElement.parentElement.remove()">OK</button>
        </div>
    `;
    
    // Add to body
    document.body.appendChild(popup);
    
    // Show popup
    setTimeout(() => {
        popup.classList.add('show');
    }, 10);
    
    // Auto-remove after 5 seconds
    setTimeout(() => {
        if (popup.parentElement) {
            popup.remove();
        }
    }, 5000);
}

// Function to show success popup
function showSuccessPopup(message) {
    // Remove any existing success popup
    const existingPopup = document.querySelector('.success-popup');
    if (existingPopup) {
        existingPopup.remove();
    }
    
    // Create success popup
    const popup = document.createElement('div');
    popup.className = 'success-popup';
    popup.innerHTML = `
        <div class="success-content">
            <i class="fas fa-check-circle"></i>
            <p>${message}</p>
            <button onclick="this.parentElement.parentElement.remove()">OK</button>
        </div>
    `;
    
    // Add to body
    document.body.appendChild(popup);
    
    // Show popup
    setTimeout(() => {
        popup.classList.add('show');
    }, 10);
    
    // Auto-remove after 5 seconds
    setTimeout(() => {
        if (popup.parentElement) {
            popup.remove();
        }
    }, 5000);
}

// Toggle play menu visibility
function togglePlayMenu() {
    const menu = document.querySelector('.play-menu');
    menu.classList.toggle('show');
    
    // Close menu when clicking outside
    document.addEventListener('click', function closeMenu(e) {
        if (!e.target.closest('.play-menu-container')) {
            menu.classList.remove('show');
            document.removeEventListener('click', closeMenu);
        }
    });
}

// Close play menu when clicking outside
document.addEventListener('click', function(e) {
    const menu = document.querySelector('.play-menu');
    const container = document.querySelector('.play-menu-container');
    if (menu && container && !container.contains(e.target)) {
        menu.classList.remove('show');
    }
});

// Add search functionality
setupOpponentSearch();

// Function to setup opponent search functionality
function setupOpponentSearch() {
    const searchInput = document.getElementById('opponentSearch');
    const searchResults = document.getElementById('searchResults');
    const createBtn = document.getElementById('createInvitationBtn');
    let selectedOpponent = null;
    let searchTimeout = null;
    
    if (!searchInput) return;
    
    // Get current username for exclusion
    const usernameElement = document.querySelector('span[data-username="true"]');
    const currentUsername = usernameElement ? usernameElement.textContent : '';
    
    // Search as user types
    searchInput.addEventListener('input', function() {
        const query = this.value.trim();
        
        // Clear previous timeout
        if (searchTimeout) {
            clearTimeout(searchTimeout);
        }
        
        // Clear results if query is empty
        if (query.length === 0) {
            searchResults.innerHTML = '';
            searchResults.style.display = 'none';
            selectedOpponent = null;
            createBtn.disabled = true;
            return;
        }
        
        // Debounce search requests
        searchTimeout = setTimeout(() => {
            if (query.length >= 2) { // Only search if query has at least 2 characters
                searchUsers(query, currentUsername);
            }
        }, 300);
    });
    
    // Handle keyboard navigation
    searchInput.addEventListener('keydown', function(e) {
        const results = searchResults.querySelectorAll('.search-result-item');
        const currentIndex = Array.from(results).findIndex(item => item.classList.contains('selected'));
        
        if (e.key === 'ArrowDown') {
            e.preventDefault();
            const nextIndex = (currentIndex + 1) % results.length;
            selectSearchResult(nextIndex, results);
        } else if (e.key === 'ArrowUp') {
            e.preventDefault();
            const prevIndex = currentIndex <= 0 ? results.length - 1 : currentIndex - 1;
            selectSearchResult(prevIndex, results);
        } else if (e.key === 'Enter') {
            e.preventDefault();
            if (currentIndex >= 0 && results[currentIndex]) {
                selectOpponent(results[currentIndex].dataset.username);
            }
        } else if (e.key === 'Escape') {
            searchResults.style.display = 'none';
            searchInput.blur();
        }
    });
    
    // Close search results when clicking outside
    document.addEventListener('click', function(e) {
        if (!searchInput.contains(e.target) && !searchResults.contains(e.target)) {
            searchResults.style.display = 'none';
        }
    });
    
    // Function to search users
    function searchUsers(query, currentUsername) {
        const params = new URLSearchParams({
            q: query,
            currentUser: currentUsername
        });
        
        fetch(`/api/users/search?${params}`)
            .then(response => response.json())
            .then(users => {
                displaySearchResults(users);
            })
            .catch(error => {
                
                searchResults.innerHTML = '<div class="search-error">Error searching users</div>';
                searchResults.style.display = 'block';
            });
    }
    
    // Function to display search results
    function displaySearchResults(users) {
        if (users.length === 0) {
            searchResults.innerHTML = '<div class="no-results">No users found</div>';
        } else {
            searchResults.innerHTML = users.map(user => `
                <div class="search-result-item" data-username="${user.username}">
                    <span class="username">${user.username}</span>
                </div>
            `).join('');
            
            // Add click handlers to results
            searchResults.querySelectorAll('.search-result-item').forEach(item => {
                item.addEventListener('click', function() {
                    selectOpponent(this.dataset.username);
                });
            });
        }
        searchResults.style.display = 'block';
    }
    
    // Function to select a search result
    function selectSearchResult(index, results) {
        results.forEach(item => item.classList.remove('selected'));
        if (results[index]) {
            results[index].classList.add('selected');
        }
    }
    
    // Function to select an opponent
    function selectOpponent(username) {
        selectedOpponent = username;
        searchInput.value = username;
        searchResults.style.display = 'none';
        // Use the new form validation function
        checkFormValidity();
    }
}

// Function to create invitation
function createInvitation() {
    // Get form values
    const opponentSearch = document.getElementById('opponentSearch');
    const timeControlValue = document.getElementById('timeControl').value; // e.g., "3|2"
    const playerColor = document.querySelector('input[name="playerColor"]:checked').value;
    // Validate form
    if (!opponentSearch || !opponentSearch.value.trim()) {
        showErrorPopup('Please select an opponent');
        return;
    }
    if (!timeControlValue) {
        showErrorPopup('Please select a time control');
        return;
    }
    // Parse minutes and increment
    let [minutes, increment] = timeControlValue.split('|');
    minutes = parseFloat(minutes); // Handles "0.5" for 30 sec, etc.
    increment = parseInt(increment); // Increment in seconds
    // Get username
    const usernameElement = document.querySelector('span[data-username="true"]');
    if (!usernameElement) {
        showErrorPopup('User not found. Please try logging in again.');
        return;
    }
    const username = usernameElement.textContent;
    const opponentUsername = opponentSearch.value.trim();
    // Create the invitation request body
    const requestBody = {
        username: username,
        opponentUsername: opponentUsername,
        timeControlMinutes: minutes, // Pass minutes
        incrementSeconds: increment, // Pass increment
        playerColor: playerColor // Pass the selected color
    };
    // Close popup first
    closeGameCreationPopup();
    // Call the create invitation endpoint
    fetch('/api/invitations/create', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestBody)
    })
    .then(response => {
        if (!response.ok) {
            return response.json().then(errorData => {
                showErrorPopup(errorData.message);
                throw new Error(errorData.message);
            });
        }
        return response.json();
    })
    .then(data => {
        if (!data) return; // Return if we showed an error popup
        showSuccessPopup(`Invitation sent to ${opponentUsername}! You will be redirected to the game page once they join.`);
    })
    .catch(error => {
        // Don't show another error popup here since we already showed one in the response handling
    });
}

function handleInvitationNotification(notification) {
    
    
    switch (notification.type) {
        case 'INVITATION_RECEIVED':
            showInvitationReceivedNotification(notification);
            break;
        case 'INVITATION_ACCEPTED':
            showInvitationAcceptedNotification(notification);
            break;
        case 'INVITATION_DECLINED':
            showInvitationDeclinedNotification(notification);
            break;
        case 'INVITATION_CANCELLED':
            showInvitationCancelledNotification(notification);
            break;
        case 'INVITATION_EXPIRED':
            showInvitationExpiredNotification(notification);
            break;
        default:
            
    }
}


// Function to show invitation received notification
function showInvitationReceivedNotification(notification) {
    const inviterName = notification.inviterUsername;
    const timeControl = notification.timeControlMinutes;
    const playerColor = notification.playerColor === 'white' ? 'black' : 
                      notification.playerColor === 'black' ? 'white' : 'random';
    
    const message = `${inviterName} invited you to a ${timeControl}-minute game! (You will play as ${playerColor} color)`;
    
    // Play notification sound for real-time invitations
    playNotificationSound();
    
    // Show enhanced notification for real-time invitations
    showRealTimeInvitationNotification(message, notification.invitationId);
    
    // If join dialog is open, refresh invitations
    const joinDialog = document.getElementById('joinGameDialog');
    if (joinDialog && joinDialog.classList.contains('active')) {
        const usernameElement = document.querySelector('span[data-username="true"]');
        if (usernameElement) {
            loadPendingInvitations(usernameElement.textContent);
        }
    }
}

// Function to play notification sound
function playNotificationSound() {
    try {
        const audio = new Audio('/audio/Check.wav');
        audio.volume = 0.5; // Set volume to 50%
        audio.play().catch(error => {
            
        });
    } catch (error) {
        
    }
}

// Function to show enhanced real-time invitation notification
function showRealTimeInvitationNotification(message, invitationId) {
    // Remove any existing invitation popup
    const existingPopup = document.querySelector('.invitation-popup');
    if (existingPopup) {
        existingPopup.remove();
    }
    
    // Create enhanced invitation popup with animation
    const popup = document.createElement('div');
    popup.className = 'invitation-popup real-time';
    popup.innerHTML = `
        <div class="invitation-content">
            <div class="notification-header">
                <i class="fas fa-chess animate-pulse"></i>
                <span class="real-time-badge">LIVE</span>
            </div>
            <p class="notification-message">${message}</p>
            <div class="invitation-actions">
                <button class="btn-view-invitations" onclick="viewInvitations()">
                    <i class="fas fa-eye"></i> View Invitations
                </button>
                <button class="btn-dismiss" onclick="this.parentElement.parentElement.parentElement.remove()">
                    <i class="fas fa-times"></i> Dismiss
                </button>
            </div>
        </div>
    `;
    
    // Add to body
    document.body.appendChild(popup);
    
    // Add entrance animation
    setTimeout(() => {
        popup.classList.add('show');
        // Add attention-grabbing animation
        popup.classList.add('attention');
    }, 10);
    
    // Remove attention animation after 3 seconds
    setTimeout(() => {
        popup.classList.remove('attention');
    }, 3000);
    
    // Auto-remove after 12 seconds (longer for real-time notifications)
    setTimeout(() => {
        if (popup.parentElement) {
            popup.classList.remove('show');
            setTimeout(() => {
                if (popup.parentElement) {
                    popup.remove();
                }
            }, 300);
        }
    }, 12000);
}

// Function to show invitation accepted notification
function showInvitationAcceptedNotification(notification) {
    
    
    // Get current user's username
    const usernameElement = document.querySelector('span[data-username="true"]');
    const currentUsername = usernameElement ? usernameElement.textContent : '';
    
    
    
    
    
    // Check if user is still authenticated
    if (!currentUsername) {
        
        showErrorPopup('You appear to be logged out. Please refresh the page and try again.');
        return;
    }
    
    // Determine which message to show based on current user
    let message;
    if (currentUsername === notification.inviterUsername) {
        // Current user is the inviter
        message = `${notification.inviteeUsername} accepted your invitation!`;
        
    } else if (currentUsername === notification.inviteeUsername) {
        // Current user is the invitee
        message = `You accepted ${notification.inviterUsername}'s invitation!`;
        
    } else {
        // Fallback message
        message = `Invitation accepted! Game is ready.`;
        
    }
    
    showSuccessPopup(message);
    
    // Redirect to game if gameId is provided (both users should be redirected)
    if (notification.gameId) {
        
        
        // If this is the inviter, delete the invitation after 3 seconds
        if (currentUsername === notification.inviterUsername && notification.invitationId) {
            
            setTimeout(() => {
                deleteInvitation(notification.invitationId);
            }, 3000); // 3 second delay before deletion
        }
        
        setTimeout(() => {
            
            window.location.href = `/games/${notification.gameId}`;
        }, 2000);
    } else {
        
    }
}

// Function to delete an accepted invitation
function deleteInvitation(invitationId) {
    
    
    
    
    
    fetch(`/api/invitations/delete/${invitationId}`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (response.ok) {
            
            return response.text();
        } else {
            
            return response.text().then(text => {
                
                throw new Error(`HTTP ${response.status}: ${text}`);
            });
        }
    })
    .then(responseText => {
        
    })
    .catch(error => {
        
    });
}

// Function to show invitation declined notification
function showInvitationDeclinedNotification(notification) {
    const message = `${notification.inviteeUsername} declined your invitation.`;
    showErrorPopup(message);
}

// Function to show invitation cancelled notification
function showInvitationCancelledNotification(notification) {
    const message = `${notification.inviterUsername} cancelled the invitation.`;
    showErrorPopup(message);
    
    // If join dialog is open, refresh invitations
    const joinDialog = document.getElementById('joinGameDialog');
    if (joinDialog && joinDialog.classList.contains('active')) {
        const usernameElement = document.querySelector('span[data-username="true"]');
        if (usernameElement) {
            loadPendingInvitations(usernameElement.textContent);
        }
    }
}

// Function to show invitation expired notification
function showInvitationExpiredNotification(notification) {
    const message = 'An invitation has expired.';
    showErrorPopup(message);
    
    // If join dialog is open, refresh invitations
    const joinDialog = document.getElementById('joinGameDialog');
    if (joinDialog && joinDialog.classList.contains('active')) {
        const usernameElement = document.querySelector('span[data-username="true"]');
        if (usernameElement) {
            loadPendingInvitations(usernameElement.textContent);
        }
    }
}

// Add polling mechanism to check for accepted invitations
let invitationPollingInterval = null;
let lastInvitationCount = 0; // Track the number of invitations to detect new ones

// Function to start polling for accepted invitations
function startInvitationPolling() {
    const username = getCurrentUsername();
    if (!username) {
        
        return;
    }
    
    // Check every 5 seconds for new invitations and accepted invitations
    invitationPollingInterval = setInterval(() => {
        checkForNewInvitations(username);
        checkForAcceptedInvitations(username);
    }, 5000);
}

// Function to check for new invitations (fallback for WebSocket)
function checkForNewInvitations(username) {
    fetch(`/api/invitations/pending/${username}`)
        .then(response => response.json())
        .then(invitations => {
            const currentCount = invitations.length;
            
            // If we have more invitations than before, show notification for new ones
            if (currentCount > lastInvitationCount && lastInvitationCount > 0) {
                const newInvitations = invitations.slice(0, currentCount - lastInvitationCount);
                newInvitations.forEach(invitation => {
                    const inviterName = invitation.inviterUsername;
                    const timeControl = invitation.timeControlMinutes;
                    const playerColor = invitation.playerColor === 'white' ? 'black' : 
                                      invitation.playerColor === 'black' ? 'white' : 'random';
                    
                    const message = `${inviterName} invited you to a ${timeControl}-minute game! (You will play as ${playerColor} color)`;
                    
                    // Play notification sound for new invitations
                    playNotificationSound();
                    
                    // Show notification for new invitation
                    showRealTimeInvitationNotification(message, invitation.id);
                });
            }
            
            lastInvitationCount = currentCount;
        })
        .catch(error => {
        });
}

// Function to check for accepted invitations
function checkForAcceptedInvitations(username) {
    fetch(`/api/invitations/sent/${username}`)
        .then(response => response.json())
        .then(invitations => {
            // Look for accepted invitations
            const acceptedInvitation = invitations.find(inv => 
                inv.status === 'ACCEPTED' && inv.gameId
            );
            
            if (acceptedInvitation) {
                showSuccessPopup('Your invitation was accepted! Redirecting to game...');
                
                // Delete the invitation after 3 seconds
                setTimeout(() => {
                    deleteInvitation(acceptedInvitation.id);
                }, 3000); // 3 second delay before deletion
                
                setTimeout(() => {
                    window.location.href = `/games/${acceptedInvitation.gameId}`;
                }, 2000);
                stopInvitationPolling();
            }
        })
        .catch(error => {
        });
}

// Function to stop polling
function stopInvitationPolling() {
    if (invitationPollingInterval) {
        clearInterval(invitationPollingInterval);
        invitationPollingInterval = null;
    }
}

// Function to check for pending invitations and show notifications
function checkForPendingInvitationsOnLoad() {
    const username = getCurrentUsername();
    if (!username) {
        return;
    }
    
    // Fetch pending invitations
    fetch(`/api/invitations/pending/${username}`)
        .then(response => response.json())
        .then(invitations => {
            // Initialize the invitation count for polling
            lastInvitationCount = invitations.length;
            
            if (invitations && invitations.length > 0) {
                // Show notification for each pending invitation with a delay
                invitations.forEach((invitation, index) => {
                    setTimeout(() => {
                        const inviterName = invitation.inviterUsername;
                        const timeControl = invitation.timeControlMinutes;
                        const playerColor = invitation.playerColor === 'white' ? 'black' : 
                                          invitation.playerColor === 'black' ? 'white' : 'random';
                        
                        const message = `${inviterName} invited you to a ${timeControl}-minute game! (You will play as ${playerColor} color)`;
                        showInvitationNotification(message);
                    }, index * 2000); // 2 second delay between each notification
                });
            }
        })
        .catch(error => {
        });
}


// Function to show clickable invitation notification
function showInvitationNotification(message) {
    // Remove any existing invitation popup
    const existingPopup = document.querySelector('.invitation-popup');
    if (existingPopup) {
        existingPopup.remove();
    }
    
    // Create invitation popup
    const popup = document.createElement('div');
    popup.className = 'invitation-popup';
    popup.innerHTML = `
        <div class="invitation-content">
            <i class="fas fa-chess"></i>
            <p>${message}</p>
            <div class="invitation-actions">
                <button class="btn-view-invitations" onclick="viewInvitations()">View Invitations</button>
                <button class="btn-dismiss" onclick="this.parentElement.parentElement.parentElement.remove()">Dismiss</button>
            </div>
        </div>
    `;
    
    // Add to body
    document.body.appendChild(popup);
    
    // Show popup
    setTimeout(() => {
        popup.classList.add('show');
    }, 10);
    
    // Auto-remove after 8 seconds (longer than regular notifications)
    setTimeout(() => {
        if (popup.parentElement) {
            popup.remove();
        }
    }, 8000);
}

// Function to view invitations (opens join dialog)
function viewInvitations() {
    // Remove the notification
    const popup = document.querySelector('.invitation-popup');
    if (popup) {
        popup.remove();
    }
    
    // Open the join game dialog
    showJoinGameDialog();
}

// Function to handle WebSocket disconnection and reconnection
function handleDisconnect() {
    
    if (stompClient) {
        stompClient.disconnect();
        stompClient = null;
    }
    
    // Attempt to reconnect if we haven't exceeded max attempts
    if (reconnectAttempts < maxReconnectAttempts) {
        reconnectAttempts++;
        
        // Wait before attempting to reconnect (exponential backoff)
        const delay = Math.min(1000 * Math.pow(2, reconnectAttempts - 1), 10000);
        setTimeout(() => {
            if (document.body.classList.contains('authenticated')) {
                connect();
            }
        }, delay);
    } else {
        // Show user-friendly error message
        showErrorPopup('Connection lost. Please refresh the page to restore real-time notifications.');
    }
}

// Function to setup custom dropdown
function setupCustomDropdown() {
    const trigger = document.getElementById('timeControlTrigger');
    const menu = document.getElementById('timeControlMenu');
    const hiddenInput = document.getElementById('timeControl');
    const dropdownText = trigger.querySelector('.dropdown-text');
    
    if (!trigger || !menu || !hiddenInput) return;
    
    // Toggle dropdown on trigger click
    trigger.addEventListener('click', function(e) {
        e.preventDefault();
        e.stopPropagation();
        
        // Toggle active state
        trigger.classList.toggle('active');
        menu.classList.toggle('show');
    });
    
    // Handle option selection
    menu.addEventListener('click', function(e) {
        const option = e.target.closest('.dropdown-option');
        if (!option) return;
        
        const value = option.dataset.value;
        const text = option.textContent;
        
        // Update hidden input value
        hiddenInput.value = value;
        
        // Update display text
        dropdownText.textContent = text;
        
        // Update visual selection
        menu.querySelectorAll('.dropdown-option').forEach(opt => {
            opt.classList.remove('selected');
        });
        option.classList.add('selected');
        
        // Close dropdown
        trigger.classList.remove('active');
        menu.classList.remove('show');
        
        // Enable create button if opponent is selected
        checkFormValidity();
    });
    
    // Close dropdown when clicking outside
    document.addEventListener('click', function(e) {
        if (!trigger.contains(e.target) && !menu.contains(e.target)) {
            trigger.classList.remove('active');
            menu.classList.remove('show');
        }
    });
    
    // Close dropdown on escape key
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            trigger.classList.remove('active');
            menu.classList.remove('show');
        }
    });
}

// Function to check form validity and enable/disable create button
function checkFormValidity() {
    const opponentSearch = document.getElementById('opponentSearch');
    const timeControl = document.getElementById('timeControl');
    const createBtn = document.getElementById('createInvitationBtn');
    
    if (!opponentSearch || !timeControl || !createBtn) return;
    
    const opponentSelected = opponentSearch.value.trim() !== '';
    const timeControlSelected = timeControl.value.trim() !== '';
    
    createBtn.disabled = !(opponentSelected && timeControlSelected);
    
    // Update button text based on opponent selection
    if (opponentSelected) {
        createBtn.textContent = `Send Invitation to ${opponentSearch.value.trim()}`;
    } else {
        createBtn.textContent = 'Send Invitation';
    }
}