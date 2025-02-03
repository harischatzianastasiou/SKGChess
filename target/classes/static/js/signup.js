// Wait for DOM to be ready
document.addEventListener('DOMContentLoaded', function() {
    // Get the form element
    const form = document.getElementById('signupForm');
    
    // Only add event listener if we're on the signup page
    if (!form) return;
    
    // Listen for form submission
    form.addEventListener('submit', async function(event) {
        // Prevent default form submission
        event.preventDefault();
        
        // Get form values, collect all the data from the form in one object
        const userData = {
            username: document.getElementById('username').value,
            email: document.getElementById('email').value,
            password: document.getElementById('password').value
        };
        
        // Basic validation
        const confirmPassword = document.getElementById('confirmPassword').value;
        if (userData.password !== confirmPassword) {
            alert('Passwords do not match');
            return;
        }

        // Validate username length
        if (userData.username.length < 3 || userData.username.length > 50) {
            alert('Username must be between 3 and 50 characters');
            return;
        }

        // Validate email format
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(userData.email)) {
            alert('Please enter a valid email address');
            return;
        }
        
        try {
            // Send data to server
            const response = await fetch('/req/signup', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(userData)
            });
            
            if (response.ok) {
                // If successful, parse the response to see what the server sent back
                const player = await response.json();
                console.log('Created player:', player);
                alert('Signup successful!');
                window.location.href = '/req/login';
            } else {
                // If there's an error, try to get the error message
                const errorText = await response.text();
                console.error('Server error:', errorText);
                alert('Signup failed: ' + (errorText || 'Unknown error'));
            }
        } catch (error) {
            console.error('Network error:', error);
            alert('An error occurred. Please try again.');
        }
    });
});