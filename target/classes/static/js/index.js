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
        alert(`Error starting matchmaking: ${error.message}`);
    });
}

function joinGame() {
    // Get the username from the page
    const username = document.getElementById('username').textContent;
    console.log("Username for joining game:", username);
    
    // First fetch the user ID using the username
    fetch(`api/users/${username}`)
        .then(response => {
            if (!response.ok) {
                throw new Error(`Failed to fetch user: ${response.status} ${response.statusText}`);
            }
            return response.json();
        })
        .then(user => {
            console.log("User data:", user);
            if (!user || !user.userId) {
                throw new Error('User ID not found in response');
            }
            const userId = user.userId;
            console.log("User ID for joining game:", userId);
            
            // Call the join game endpoint
            return fetch(`/api/games/join/${userId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            });
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
            window.location.href = `/api/games/${data.id}`;
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