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
        const confirmPassword = document.getElementById('confirm-password').value;
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
            const response = await fetch('/api/users/signup', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify(userData)
            });
            
            // Check the content type of the response
            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
                // Handle JSON response
                const data = await response.json();
                if (response.ok) {
                    console.log('Created user:', data);
                    alert('Signup successful!');
                    window.location.href = '/login';
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
    });
});