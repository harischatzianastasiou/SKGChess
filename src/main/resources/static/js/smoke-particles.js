/**
 * Smoke Particles Animation
 * Creates realistic smoke particle effects that rise and disperse
 */

// Wait for the DOM to be fully loaded before initializing
document.addEventListener('DOMContentLoaded', function() {
    // Initialize the smoke effect
    initSmokeEffect();
});

/**
 * Initializes the smoke particle effect
 * Creates the container and starts generating particles
 */
function initSmokeEffect() {
    // Create the container for all smoke particles
    const smokeContainer = document.createElement('div');
    smokeContainer.className = 'smoke-container';
    document.body.appendChild(smokeContainer);
    
    // Set the number of particles to create
    const particleCount = 30; // Increased number of particles
    
    // Create initial particles
    for (let i = 0; i < particleCount; i++) {
        createSmokeParticle(smokeContainer);
    }
    
    // Continuously create new particles at random intervals
    setInterval(() => {
        createSmokeParticle(smokeContainer);
    }, 1000); // Create a new particle every second
}

/**
 * Creates a single smoke particle with random properties
 * @param {HTMLElement} container - The container element for the particle
 */
function createSmokeParticle(container) {
    // Create the particle element
    const particle = document.createElement('div');
    particle.className = 'smoke-particle';
    
    // Randomly assign size class
    const sizes = ['small', 'medium', 'large'];
    const randomSize = sizes[Math.floor(Math.random() * sizes.length)];
    particle.classList.add(randomSize);
    
    // Randomly assign color class
    const colors = ['light', 'dark', 'brown'];
    const randomColor = colors[Math.floor(Math.random() * colors.length)];
    particle.classList.add(randomColor);
    
    // Set random position across the entire screen
    const startPositionX = Math.random() * window.innerWidth;
    const startPositionY = Math.random() * window.innerHeight;
    particle.style.left = `${startPositionX}px`;
    particle.style.top = `${startPositionY}px`;
    
    // Add random rotation and skew for more realistic smoke
    const rotation = Math.random() * 360;
    const skew = Math.random() * 20 - 10; // Random skew between -10 and 10 degrees
    particle.style.setProperty('--rotation', `${rotation}deg`);
    particle.style.setProperty('--skew', `${skew}deg`);
    
    // Add the particle to the container
    container.appendChild(particle);
    
    // Calculate random animation duration (between 8-15 seconds)
    const duration = 8 + Math.random() * 7;
    
    // Apply the rise animation
    particle.style.animation = `smokeRise ${duration}s ease-out forwards`;
    
    // Apply the drift animation with a slight delay
    setTimeout(() => {
        particle.style.animation = `smokeRise ${duration}s ease-out forwards, 
                                    smokeDrift ${3 + Math.random() * 4}s ease-in-out infinite`;
    }, 100);
    
    // Remove the particle after animation completes
    setTimeout(() => {
        if (particle.parentNode === container) {
            container.removeChild(particle);
        }
    }, duration * 1000);
} 