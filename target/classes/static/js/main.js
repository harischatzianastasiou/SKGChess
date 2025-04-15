// Modern Chess Website Interactions

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    initializeAnimations();
    initializeStats();
    initializeLiveGames();
    initializeQuickAccess();
});

// Initialize animations for elements
function initializeAnimations() {
    // Animate elements on scroll
    const animatedElements = document.querySelectorAll('.animate-on-scroll');
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('animated');
                observer.unobserve(entry.target);
            }
        });
    });

    animatedElements.forEach(element => observer.observe(element));

    // Add hover effects to cards
    document.querySelectorAll('.chess-card').forEach(card => {
        card.addEventListener('mousemove', (e) => {
            const rect = card.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;

            card.style.setProperty('--mouse-x', `${x}px`);
            card.style.setProperty('--mouse-y', `${y}px`);
        });
    });
}

// Initialize stats widget with counter animations
function initializeStats() {
    const stats = {
        gamesPlayed: 150,
        puzzlesSolved: 75,
        rating: 1500
    };

    // Animate numbers counting up
    Object.entries(stats).forEach(([key, value]) => {
        const element = document.querySelector(`#${key}`);
        if (element) {
            animateNumber(element, 0, value, 2000);
        }
    });
}

// Animate number counting up
function animateNumber(element, start, end, duration) {
    const range = end - start;
    const increment = end > start ? 1 : -1;
    const stepTime = Math.abs(Math.floor(duration / range));
    let current = start;
    
    const timer = setInterval(() => {
        current += increment;
        element.textContent = current;
        if (current === end) {
            clearInterval(timer);
        }
    }, stepTime);
}

// Initialize live games counter and updates
function initializeLiveGames() {
    const liveGamesCount = document.querySelector('.live-games-count');
    if (liveGamesCount) {
        // Simulate live games updates
        setInterval(() => {
            const currentGames = parseInt(liveGamesCount.textContent);
            const change = Math.floor(Math.random() * 3) - 1; // Random change between -1 and 1
            const newCount = Math.max(0, currentGames + change);
            liveGamesCount.textContent = newCount;
        }, 5000);
    }
}

// Initialize quick access menu
function initializeQuickAccess() {
    const quickAccess = document.querySelector('.quick-access');
    if (quickAccess) {
        const button = quickAccess.querySelector('.quick-access-button');
        const menu = quickAccess.querySelector('.quick-access-menu');

        button.addEventListener('click', () => {
            menu.classList.toggle('active');
            button.classList.toggle('active');
        });

        // Close menu when clicking outside
        document.addEventListener('click', (e) => {
            if (!quickAccess.contains(e.target)) {
                menu.classList.remove('active');
                button.classList.remove('active');
            }
        });
    }
}

// Typing animation for welcome message
function initializeTypingAnimation() {
    const welcomeText = document.querySelector('.welcome-text');
    if (welcomeText) {
        const text = welcomeText.textContent;
        welcomeText.textContent = '';
        let i = 0;

        const typing = setInterval(() => {
            if (i < text.length) {
                welcomeText.textContent += text.charAt(i);
                i++;
            } else {
                clearInterval(typing);
            }
        }, 100);
    }
}

// Initialize particle background
function initializeParticles() {
    const canvas = document.querySelector('#particles');
    if (canvas) {
        const ctx = canvas.getContext('2d');
        const particles = [];

        // Particle animation code here
        function createParticle() {
            return {
                x: Math.random() * canvas.width,
                y: Math.random() * canvas.height,
                size: Math.random() * 3 + 1,
                speedX: Math.random() * 2 - 1,
                speedY: Math.random() * 2 - 1
            };
        }

        // Initialize particles
        for (let i = 0; i < 50; i++) {
            particles.push(createParticle());
        }

        // Animation loop
        function animate() {
            ctx.clearRect(0, 0, canvas.width, canvas.height);
            
            particles.forEach(particle => {
                particle.x += particle.speedX;
                particle.y += particle.speedY;

                if (particle.x < 0 || particle.x > canvas.width) particle.speedX *= -1;
                if (particle.y < 0 || particle.y > canvas.height) particle.speedY *= -1;

                ctx.beginPath();
                ctx.arc(particle.x, particle.y, particle.size, 0, Math.PI * 2);
                ctx.fillStyle = 'rgba(255, 255, 255, 0.1)';
                ctx.fill();
            });

            requestAnimationFrame(animate);
        }

        animate();
    }
} 