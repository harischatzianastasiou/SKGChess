// Bouncing Logo Animation Handler
document.addEventListener('DOMContentLoaded', function() {
    // Find the view my games card container
    const gamesCard = document.querySelector('.action-card:first-child');
    if (!gamesCard) return; // Exit if card not found
    
    // Create container within the games card
    const container = document.createElement('div');
    container.className = 'bouncing-container';
    gamesCard.appendChild(container);

    // Create logo
    const logo = document.createElement('img');
    logo.src = '/images/skgchessicon.png';
    logo.className = 'bouncing-logo';
    logo.alt = 'SKG Chess Logo';
    container.appendChild(logo);

    // Animation variables with adjusted speed and random direction
    let x = 0;  // Start at the actual left edge
    let y = Math.random() * (gamesCard.offsetHeight - 60);
    let speed = 0.5;
    let dx = speed * (Math.random() < 0.5 ? -1 : 1);  // Random direction
    let dy = speed * (Math.random() < 0.5 ? -1 : 1);  // Random direction
    let lastTime = 0;
    let isAnimating = true;

    // Set initial position
    logo.style.position = 'absolute';
    logo.style.left = '0px';
    logo.style.top = y + 'px';

    function animate(currentTime) {
        if (!isAnimating) return;

        // Calculate time delta
        if (!lastTime) lastTime = currentTime;
        const delta = currentTime - lastTime;
        lastTime = currentTime;

        // Update position
        x += dx;
        y += dy;

        // Check boundaries of the games card - only bounce at edges
        const maxX = gamesCard.offsetWidth - logo.offsetWidth;
        const maxY = gamesCard.offsetHeight - logo.offsetHeight;
        let collision = false;

        // Only check for collisions at the edges
        if (x >= maxX) {
            x = maxX;
            dx = -dx;
            collision = true;
        } else if (x <= 0) {
            x = 0;
            dx = -dx;
            collision = true;
        }

        if (y >= maxY) {
            y = maxY;
            dy = -dy;
            collision = true;
        } else if (y <= 0) {
            y = 0;
            dy = -dy;
            collision = true;
        }

        // Handle collision effect
        if (collision) {
            logo.classList.add('hit-edge');
            setTimeout(() => logo.classList.remove('hit-edge'), 200);
        }

        // Update position using left/top instead of transform
        logo.style.left = x + 'px';
        logo.style.top = y + 'px';

        requestAnimationFrame(animate);
    }

    // Handle card hover
    gamesCard.addEventListener('mouseenter', () => {
        isAnimating = false;
    });

    gamesCard.addEventListener('mouseleave', () => {
        isAnimating = true;
        lastTime = 0;
        requestAnimationFrame(animate);
    });

    // Handle card resize
    const resizeObserver = new ResizeObserver(() => {
        const maxX = gamesCard.offsetWidth - logo.offsetWidth;
        const maxY = gamesCard.offsetHeight - logo.offsetHeight;
        
        // Keep logo in bounds after resize
        x = Math.min(Math.max(x, 0), maxX);
        y = Math.min(y, maxY);
        
        // Update position
        logo.style.left = x + 'px';
        logo.style.top = y + 'px';
    });
    
    resizeObserver.observe(gamesCard);

    // Start animation when image loads
    logo.onload = () => requestAnimationFrame(animate);
}); 