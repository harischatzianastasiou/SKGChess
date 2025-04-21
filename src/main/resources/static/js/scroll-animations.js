/**
 * Scroll Animations JavaScript
 * Handles smooth scrolling, section transitions, and scroll indicator functionality
 */

// Wait for the DOM to be fully loaded
document.addEventListener('DOMContentLoaded', function() {
    // Get all sections on the page
    const sections = document.querySelectorAll('section');
    
    // Get the footer element
    const footer = document.querySelector('.site-footer');
    
    // Get the scroll indicator element
    const scrollIndicator = document.querySelector('.scroll-indicator');
    
    // Create an array of all navigable elements (sections + footer)
    const navigableElements = Array.from(sections);
    if (footer) {
        navigableElements.push(footer);
    }
    
    // Initialize the first section as visible
    if (navigableElements.length > 0) {
        navigableElements[0].classList.add('section-visible');
    }
    
    // Track current section index
    let currentSectionIndex = 0;
    let isScrolling = false;
    
    // Function to scroll to a specific element
    function scrollToElement(element, index) {
        // Prevent multiple scroll events
        if (isScrolling) return;
        isScrolling = true;
        
        // Update current section index
        currentSectionIndex = index;
        
        // Smooth scroll to the element
        element.scrollIntoView({ behavior: 'smooth' });
        
        // Add visible class to the element after scrolling
        setTimeout(() => {
            element.classList.add('section-visible');
            isScrolling = false;
        }, 500); // Increased timeout to ensure animation completes
    }
    
    // Function to scroll to the next element
    function scrollToNextElement() {
        if (currentSectionIndex < navigableElements.length - 1) {
            scrollToElement(navigableElements[currentSectionIndex + 1], currentSectionIndex + 1);
        }
    }
    
    // Function to scroll to the previous element
    function scrollToPreviousElement() {
        if (currentSectionIndex > 0) {
            scrollToElement(navigableElements[currentSectionIndex - 1], currentSectionIndex - 1);
        }
    }
    
    // Add click event to the scroll indicator
    if (scrollIndicator) {
        scrollIndicator.addEventListener('click', function() {
            scrollToNextElement();
        });
    }
    
    // Handle wheel events for element-by-element scrolling
    window.addEventListener('wheel', function(event) {
        // Prevent default scroll behavior
        event.preventDefault();
        
        // Determine scroll direction
        const scrollDown = event.deltaY > 0;
        
        // Scroll to next or previous element based on direction
        if (scrollDown) {
            scrollToNextElement();
        } else {
            scrollToPreviousElement();
        }
    }, { passive: false });
    
    // Handle touch events for mobile devices
    let touchStartY = 0;
    
    window.addEventListener('touchstart', function(event) {
        touchStartY = event.touches[0].clientY;
    }, { passive: true });
    
    window.addEventListener('touchmove', function(event) {
        // Prevent default scroll behavior
        event.preventDefault();
        
        // Calculate touch direction
        const touchEndY = event.touches[0].clientY;
        const touchDiff = touchStartY - touchEndY;
        
        // Determine if it's a significant swipe
        if (Math.abs(touchDiff) > 50) {
            if (touchDiff > 0) {
                // Swipe up - go to next element
                scrollToNextElement();
            } else {
                // Swipe down - go to previous element
                scrollToPreviousElement();
            }
            
            // Update touch start position
            touchStartY = touchEndY;
        }
    }, { passive: false });
    
    // Add keyboard navigation for accessibility
    document.addEventListener('keydown', function(event) {
        // Check for arrow down key
        if (event.key === 'ArrowDown') {
            scrollToNextElement();
        }
        
        // Check for arrow up key
        if (event.key === 'ArrowUp') {
            scrollToPreviousElement();
        }
        
        // Check for space key
        if (event.key === ' ' && !event.repeat) {
            scrollToNextElement();
        }
    });
    
    // Add navigation dots to the page
    const navDots = document.createElement('div');
    navDots.className = 'nav-dots';
    navDots.style.position = 'fixed';
    navDots.style.right = '20px';
    navDots.style.top = '50%';
    navDots.style.transform = 'translateY(-50%)';
    navDots.style.zIndex = '100';
    navDots.style.display = 'flex';
    navDots.style.flexDirection = 'column';
    navDots.style.gap = '10px';
    
    // Create a dot for each navigable element
    navigableElements.forEach((element, index) => {
        const dot = document.createElement('div');
        dot.className = 'nav-dot' + (index === 0 ? ' active' : '');
        dot.setAttribute('data-section', index);
        
        // Add click event to each dot
        dot.addEventListener('click', function() {
            scrollToElement(navigableElements[index], index);
        });
        
        navDots.appendChild(dot);
    });
    
    // Add the navigation dots to the page
    document.body.appendChild(navDots);
    
    // Update active dot when scrolling
    function updateActiveDot() {
        const dots = document.querySelectorAll('.nav-dot');
        dots.forEach((dot, index) => {
            if (index === currentSectionIndex) {
                dot.classList.add('active');
            } else {
                dot.classList.remove('active');
            }
        });
    }
    
    // Update active dot when scrolling to an element
    const originalScrollToElement = scrollToElement;
    scrollToElement = function(element, index) {
        originalScrollToElement(element, index);
        updateActiveDot();
    };
}); 