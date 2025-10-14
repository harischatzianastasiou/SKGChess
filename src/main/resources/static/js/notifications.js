// Function to show notifications
function showNotification(message, type = 'info') {
    // Create notification element
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    
    // Add icon based on type
    let icon = '';
    switch(type) {
        case 'success':
            icon = 'check-circle';
            break;
        case 'error':
            icon = 'exclamation-circle';
            break;
        default:
            icon = 'info-circle';
    }
    
    // Set notification content
    notification.innerHTML = `
        <i class="fas fa-${icon}"></i>
        <span>${message}</span>
    `;
    
    // Add to document
    document.body.appendChild(notification);
    
    // Remove after 3 seconds
    setTimeout(() => {
        notification.style.animation = 'fadeOut 0.3s ease-out forwards';
        setTimeout(() => {
            notification.remove();
        }, 300);
    }, 3000);
}

// Function to handle sign out
function handleSignOut() {

    // Wait for 1.5 seconds to show the notification before submitting the form
    setTimeout(() => {
        // Create and submit a form to trigger the logout
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = '/logout';
        document.body.appendChild(form);
        form.submit();
    }, 400);
}

// Check for URL parameters on page load
document.addEventListener('DOMContentLoaded', function() {
    const urlParams = new URLSearchParams(window.location.search);
    
    // Check for logout parameter
    if (urlParams.has('logout')) {
        showNotification(getTranslation('successfullyLoggedOut'), 'success');
        // Clean up the URL
        window.history.replaceState({}, document.title, window.location.pathname);
    }
    
    // Check for login parameter
    if (urlParams.has('login')) {
        showNotification(getTranslation('successfullyLoggedIn'), 'success');
        // Clean up the URL
        window.history.replaceState({}, document.title, window.location.pathname);
    }
}); 