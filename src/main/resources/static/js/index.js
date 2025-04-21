// This file contains JavaScript for matchmaking functionality

let stompClient = null; // Initialize stompClient to manage WebSocket connection
let reconnectAttempts = 0; // Counter for reconnection attempts
const maxReconnectAttempts = 5; // Maximum number of reconnection attempts
const username = document.getElementById('username').textContent; // Get the username from the HTML

// Connect when page loads
// connect();

// Function to connect to the WebSocket
function connect() {
    const socket = new SockJS('/chess-websocket'); // Create a new SockJS connection
    stompClient = Stomp.over(socket); // Wrap the socket with Stomp

    stompClient.connect({}, 
        function(frame) {
            console.log('Connected: ' + frame);
            reconnectAttempts = 0;

            // Fetch user ID and set up subscriptions
            fetchUserIdByUsername(username)
                .then(userId => {
                    console.log("User ID for WebSocket:", userId);
                    
                    // Subscribe to game creation notifications
                    stompClient.subscribe('/topic/newGame/' + userId, function(message) {
                        console.log('Game created:', message.body); // Log the raw message
                        try {
                            // Parse the message body as JSON
                            const game = JSON.parse(message.body);
                            console.log('Parsed game data:', game);
                            // Handle the game creation response
                            handleGameCreated(game);
                        } catch (error) {
                            console.error('Error parsing game creation message:', error);
                            console.log('Raw message body:', message.body);
                            // Fall back to the old message handling
                            handleGameMessage(message.body);
                        }
                    });

                    // Subscribe to errors
                    stompClient.subscribe('/user/queue/errors', function(message) {
                        console.error('Error received:', message.body);
                        alert('Error: ' + message.body);
                    });
                })
                .catch(error => {
                    console.error("Failed to fetch user ID:", error);
                });
        },
        function(error) {
            console.error('STOMP error:', error);
            handleDisconnect();
        }
    );

    // socket.onclose = function() {
    //     console.log('WebSocket connection closed'); // Log when the connection is closed
    //     handleDisconnect(); // Handle disconnection
    // };
}

function newGame() {
    // Get the username from the page
    const username = document.getElementById('username').textContent;
    console.log("Username for matchmaking:", username);
    
    // Create the request body according to CreateGameRequestDTO
    const requestBody = {
        username: username
        // Other fields will use default values
    };
    
    console.log("Calling game creation endpoint with request:", requestBody);
    
    // Call the create game endpoint with the request body
    fetch('/api/games', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestBody)
    })
    .then(response => {
        console.log("Game creation response status:", response.status);
        if (!response.ok) {
            return response.text().then(text => {
                console.error("Server error details:", text);
                throw new Error(`Failed to create game: ${response.status} ${response.statusText}. ${text}`);
            });
        }
        return response.json();
    })
    .then(data => {
        console.log("Game created:", data);
        if (!data || !data.id) {
            throw new Error('Game ID not found in response');
        }
        // Redirect to the game page
        window.location.href = `/games/${data.id}`;
    })
    .catch(error => {
        console.error("Error in matchmaking:", error);
        // Display a more user-friendly error message
        const errorMessage = error.message.includes("Failed to create game") 
            ? `Error starting matchmaking: ${error.message}` 
            : `An unexpected error occurred: ${error.message}`;
        
        // Show error in a more visible way
        const errorDiv = document.getElementById('error-message') || createErrorElement();
        errorDiv.textContent = errorMessage;
        errorDiv.style.display = 'block';
        
        // Also show in alert for immediate attention
        alert(errorMessage);
    });
}

function joinGame(gameId) {
     // Create the request body according to CreateGameRequestDTO
     const requestBody = {
        username: username,
        gameId : gameId
    };
    
    console.log("Calling join game endpoint with request:", requestBody);
    // Call the join game endpoint
    fetch('/api/games/join', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestBody)
    })
    .then(response => {
        console.log("Join game response status:", response.status);
        if (!response.ok) {
            return response.text().then(text => {
                throw new Error(`Failed to join game: ${response.status} ${response.statusText}. ${text}`);
            });
        }
        return response.json();
    })
    .then(data => {
        if (!data) {
            throw new Error('No available games to join');
        }
        if (!data.id) {
            throw new Error('Game ID not found in response');
        }
        console.log("Joined game:", data);
        
        // Redirect to the game page
        window.location.href = `/games/${data.id}`;
    })
    .catch(error => {
        console.error("Error joining game:", error);
        alert(`Error joining game: ${error.message}`);
    });
}

// Function to fetch user ID by username
function fetchUserIdByUsername(username) {
    return fetch(`/api/users/${username}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('User not found');
            }
            return response.json();
        })
        .then(user => {
            console.log("User data:", user);
            if (!user || !user.userId) {
                throw new Error('User ID not found in response');
            }
            console.log("User ID:", user.userId);
            return user.userId;
        })
        .catch(error => {
            console.error("Error fetching user ID:", error);
            return null;
        });
}

// Function to handle incoming game messages
function handleGameMessage(message) {
    console.log('Handling message:', message);
    
    // If message is already a string, use it directly, otherwise stringify it
    const messageStr = typeof message === 'string' ? message : JSON.stringify(message);
    
    console.log('Processing message:', messageStr);  // Debug log
    
    try {
        // Try to parse as JSON first
        const jsonData = JSON.parse(messageStr);
        console.log('Parsed JSON data:', jsonData);
        
        if (jsonData && jsonData.id) {
            console.log('Game created with ID:', jsonData.id);
            alert('Game created successfully! Redirecting to game ' + jsonData.id);
            window.location.href = '/api/games/' + jsonData.id;
            return;
        }
    } catch (e) {
        console.log('Not valid JSON, processing as text');
    }
    
    // If not valid JSON or doesn't have expected format, process as text
    if (messageStr.includes('Game created:')) {
        const gameId = messageStr.split(':')[2].trim();  // Get the ID and trim whitespace
        console.log('Game created with ID:', gameId);  // Debug log
        alert('Game created successfully! Redirecting to game ' + gameId);
        window.location.href = '/game/' + gameId;
    } else {
        alert(messageStr);
    }
}

// Function to handle game creation response
function handleGameCreated(game) {
    console.log('Game created response:', game);
    
    // Check if game object is valid and has an ID
    if (game && game.id) {
        console.log('Game created with ID:', game.id);
        alert('Game created successfully! Redirecting to game ' + game.id);
        window.location.href = '/api/games/' + game.id;
    } else {
        // Handle error case
        console.error('Invalid game data received:', game);
        alert('Error creating game. Please try again.');
    }
}

// Function to handle disconnection and reconnection logic
// function handleDisconnect() {
//     if (stompClient !== null) {
//         stompClient.disconnect(); // Disconnect the client
//         stompClient = null; // Reset stompClient
//     }

//     if (reconnectAttempts < maxReconnectAttempts) {
//         console.log('Attempting to reconnect...'); // Log reconnection attempt
//         reconnectAttempts++; // Increment the reconnect attempts
//         setTimeout(connect, 2000 * Math.pow(2, reconnectAttempts - 1)); // Exponential backoff for reconnection
//     } else {
//         console.error('Max reconnection attempts reached'); // Log max attempts reached
//         // Show user-friendly error message
//     }
// }


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
    const body = document.body;
    const sections = document.querySelectorAll('section');
    
    // Ensure the scroll indicator is visible in the hero section
    const heroSection = document.querySelector('.hero-section');
    if (heroSection) {
        // Remove any existing scroll indicators first
        const existingIndicators = heroSection.querySelectorAll('.scroll-indicator');
        existingIndicators.forEach(indicator => indicator.remove());
        
        // Create a new scroll indicator
        const indicator = document.createElement('div');
        indicator.className = 'scroll-indicator';
        
        // Check if user is authenticated to set the appropriate text
        const isAuthenticated = document.querySelector('.user-welcome') !== null;
        indicator.innerHTML = `<div class="arrow"></div><span>${isAuthenticated ? 'View Stats' : 'Scroll Down'}</span>`;
        
        // Add click event to scroll to the next section
        indicator.addEventListener('click', () => {
            scrollToSection(1); // Scroll to the next section (index 1)
        });
        
        // Append the indicator to the hero section
        heroSection.appendChild(indicator);
        
        // Make sure it's visible
        indicator.style.opacity = '1';
        indicator.style.display = 'flex';
    }
    
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

    // Optimized scroll handling with requestAnimationFrame
    let ticking = false;
    window.addEventListener('scroll', () => {
        if (!ticking) {
            requestAnimationFrame(() => {
                body.classList.add('is-scrolling');
                
                // Update current section based on scroll position
                const scrollPosition = window.scrollY;
                const windowHeight = window.innerHeight;
                const headerHeight = 80; // Height of fixed header
                currentSection = Math.round((scrollPosition - headerHeight) / (windowHeight - headerHeight));
                
                if (scrollTimeout) {
                    clearTimeout(scrollTimeout);
                }
                
                scrollTimeout = setTimeout(() => {
                    body.classList.remove('is-scrolling');
                    scrollTimeout = null;
                }, 100);
                
                ticking = false;
            });
            ticking = true;
        }
    }, { passive: true });

    // Smooth scroll to section function
    function scrollToSection(index) {
        if (isScrolling || index < 0 || index >= sections.length) return;
        
        isScrolling = true;
        
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
        
        setTimeout(() => {
            isScrolling = false;
        }, 800); // Match this with your CSS transition duration
    }

    // Handle wheel events for smooth scrolling
    window.addEventListener('wheel', (e) => {
        // Check if the event originated from a scrollable container
        const scrollableParent = e.target.closest('.scrollable-content, .scrollable-features');
        if (scrollableParent) {
            // Allow natural scrolling within scrollable containers
            return;
        }

        if (isScrolling) return;
        
        // Prevent default scroll behavior
        e.preventDefault();
        
        if (e.deltaY > 0) {
            scrollToSection(currentSection + 1);
        } else if (e.deltaY < 0) {
            scrollToSection(currentSection - 1);
        }
    }, { passive: false });

    // Handle keyboard navigation
    window.addEventListener('keydown', (e) => {
        if (isScrolling) return;
        
        if (e.key === 'ArrowDown' || e.key === 'PageDown') {
            scrollToSection(currentSection + 1);
        } else if (e.key === 'ArrowUp' || e.key === 'PageUp') {
            scrollToSection(currentSection - 1);
        }
    });

    // Handle touch events for mobile
    let touchStartY = 0;
    window.addEventListener('touchstart', (e) => {
        // Check if the event originated from a scrollable container
        const scrollableParent = e.target.closest('.scrollable-content, .scrollable-features');
        if (scrollableParent) {
            // Allow natural touch handling within scrollable containers
            return;
        }

        touchStartY = e.touches[0].clientY;
    }, { passive: true });

    window.addEventListener('touchend', (e) => {
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
    }, { passive: true });

    // Header background change optimization
    const header = document.querySelector('.header');
    const scrollThreshold = 50;
    let lastScrollY = window.scrollY;
    let tickingHeader = false;

    window.addEventListener('scroll', () => {
        lastScrollY = window.scrollY;

        if (!tickingHeader) {
            requestAnimationFrame(() => {
                header.classList.toggle('scrolled', lastScrollY > scrollThreshold);
                tickingHeader = false;
            });
            tickingHeader = true;
        }
    }, { passive: true });

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
            
            console.log('Page Load Time:', navigationTiming.loadEventEnd - navigationTiming.startTime);
            console.log('First Paint:', paintTiming[0]?.startTime);
            console.log('First Contentful Paint:', paintTiming[1]?.startTime);
        }, 0);
    }
}

// Initialize performance monitoring
document.addEventListener('DOMContentLoaded', monitorPerformance); 