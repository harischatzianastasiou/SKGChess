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
        indicator.innerHTML = '<p>Scroll Down</p><i class="fas fa-chevron-down"></i>';
        
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
        touchStartY = e.touches[0].clientY;
    }, { passive: true });

    window.addEventListener('touchend', (e) => {
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