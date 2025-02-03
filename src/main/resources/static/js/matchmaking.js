// This file contains JavaScript for matchmaking functionality

let stompClient = null; // Initialize stompClient to manage WebSocket connection
let reconnectAttempts = 0; // Counter for reconnection attempts
const maxReconnectAttempts = 5; // Maximum number of reconnection attempts
const username = document.getElementById('username').textContent; // Get the username from the HTML

// Function to connect to the WebSocket
function connect() {
    const socket = new SockJS('/chess-websocket'); // Create a new SockJS connection
    stompClient = Stomp.over(socket); // Wrap the socket with Stomp

    stompClient.connect({}, 
        function(frame) {
            console.log('Connected: ' + frame); // Log successful connection
            // statusElement.textContent = 'Connected!';
            // statusElement.style.color = 'green';
            reconnectAttempts = 0; // Reset reconnect attempts
            
            // Update subscription paths to match server endpoints
            stompClient.subscribe('/user/session/queue/join', function(message) {
                handleGameMessage(JSON.parse(message.body));
            });

            // stompClient.subscribe('/user/queue/session', function(message) {
            //     handleGameMessage(JSON.parse(message.body)); // Handle incoming game messages
            // });

            // // Subscribe to game created
            // stompClient.subscribe('/user/session/game/create', function(message) {
            //     handleGameMessage(JSON.parse(message.body)); // Handle incoming game messages
            // });

            // Subscribe to errors
            stompClient.subscribe('/user/queue/errors', function(message) {
                console.error('Error received:', message.body);
                // Add visual feedback for the user
                alert('Error: ' + message.body);
            });

        },
        function(error) {
            console.error('STOMP error:', error); // Log STOMP errors
            handleDisconnect(); // Handle disconnection
        }
    );

    // socket.onclose = function() {
    //     console.log('WebSocket connection closed'); // Log when the connection is closed
    //     handleDisconnect(); // Handle disconnection
    // };
}

// Function to handle incoming game messages
function handleGameMessage(message) {
    switch(message.type) {
        case 'QUEUE_JOIN_SUCCESS':
            console.log('Joined matchmaking queue successfully');
            // Show user-friendly message
            alert('Joined matchmaking queue successfully');
            break;
        case 'GAME_CREATED':
            window.location.href = 'user/game/' + message.gameId; // Redirect to the game
            break;
        case 'GAME_UPDATE':
            // Handle game update
            break;
        case 'ERROR':
            console.error('Game error:', message.message); // Log game errors
            // Show user-friendly error message
            break;
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

// Function to join matchmaking
function joinMatchmaking() {
    if (stompClient && stompClient.connected) {
        stompClient.send("/app/session/queue/join", {}, JSON.stringify({
            username: username
        }));
    } else {
        console.error('Not connected to WebSocket');
    }
}

// Function to create a private game
function createPrivateGame() {
    if (stompClient && stompClient.connected) {
        stompClient.send("/app/game/create/private", {}, JSON.stringify({
            username: username // Send the username to create a private game
        }));
    } else {
        console.error('Not connected to WebSocket'); // Log if not connected
        // Show user-friendly error message
    }
}



// Connect when page loads
connect();

// Cleanup on page unload
window.onbeforeunload = function() {
    if (stompClient !== null) {
        stompClient.disconnect(); // Disconnect on page unload
    }
};

// Function to initiate matchmaking
function startMatchmaking() {
    // Logic to start matchmaking
    console.log("Matchmaking started"); // Log to console for debugging
    // Show user-friendly message
    alert("Matchmaking started");
}


// Function to cancel matchmaking
function cancelMatchmaking() {
    // Logic to cancel matchmaking
    console.log("Matchmaking canceled"); // Log to console for debugging
    // Show user-friendly message
    alert("Matchmaking canceled");
}


// Add event listeners or other initialization code here
document.addEventListener("DOMContentLoaded", function() {
    // Example: Start matchmaking when a button is clicked
    document.getElementById("startMatchButton").addEventListener("click", startMatchmaking);
}); 