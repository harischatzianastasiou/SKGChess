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
    let lastScrollTime = 0; // Track the last scroll time
    const scrollDebounceTime = 100; // Minimum time between scroll events in milliseconds
    
    // Function to scroll to a specific element
    function scrollToElement(element, index) {
        // Prevent multiple scroll events and check debounce time
        const currentTime = Date.now();
        if (isScrolling || currentTime - lastScrollTime < scrollDebounceTime) return;
        
        isScrolling = true;
        lastScrollTime = currentTime;
        
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
    
    // Function to check if we're at the bottom of a section
    function isAtBottomOfSection() {
        const currentSection = navigableElements[currentSectionIndex];
        const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
        const sectionTop = currentSection.offsetTop;
        const sectionBottom = sectionTop + currentSection.offsetHeight;
        const windowHeight = window.innerHeight;
        
        // Check if we're at the bottom of the section
        return scrollTop + windowHeight >= sectionBottom - 10; // 10px threshold
    }
    
    // Function to check if we're at the top of a section
    function isAtTopOfSection() {
        const currentSection = navigableElements[currentSectionIndex];
        const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
        const sectionTop = currentSection.offsetTop;
        
        // Check if we're at the top of the section
        return scrollTop <= sectionTop + 10; // 10px threshold
    }
    
    // Function to check if we're in a special section that needs custom handling
    function isInSpecialSection() {
        const currentSection = navigableElements[currentSectionIndex];
        return currentSection.classList.contains('quick-actions') || 
               currentSection.classList.contains('features-section');
    }
    
    // Function to check if we're in the footer
    function isInFooter() {
        return document.activeElement && document.activeElement.classList.contains('site-footer');
    }
    
    // Function to check if we're in the quick-actions title area
    function isInQuickActionsTitle(event) {
        const quickActionsSection = document.querySelector('.quick-actions');
        if (!quickActionsSection) return false;
        
        const titleElement = quickActionsSection.querySelector('.section-header');
        if (!titleElement) return false;
        
        const rect = titleElement.getBoundingClientRect();
        return rect.top <= 0 && rect.bottom >= 0;
    }
    
    // Function to check if we're in a feature card
    function isInFeatureCard(event) {
        return event.target.closest('.feature-card') !== null;
    }
    
    // Function to check if a feature card is at the top or bottom
    function isFeatureCardAtBoundary(event, isTop) {
        const featureCard = event.target.closest('.feature-card');
        if (!featureCard) return false;
        
        const cardRect = featureCard.getBoundingClientRect();
        const cardScrollTop = featureCard.scrollTop;
        const cardScrollHeight = featureCard.scrollHeight;
        const cardClientHeight = featureCard.clientHeight;
        
        if (isTop) {
            // Check if we're at the top of the feature card
            return cardScrollTop <= 10; // 10px threshold
        } else {
            // Check if we're at the bottom of the feature card
            return cardScrollTop + cardClientHeight >= cardScrollHeight - 10; // 10px threshold
        }
    }
    
    // Function to check if we're in the quick-actions section
    function isInQuickActions() {
        const currentSection = navigableElements[currentSectionIndex];
        return currentSection.classList.contains('quick-actions');
    }

    // Function to check if we're at the top of quick actions content
    function isAtQuickActionsTop() {
        const quickActionsSection = document.querySelector('.quick-actions');
        if (!quickActionsSection) return false;
        
        const contentArea = quickActionsSection.querySelector('.scrollable-content');
        if (!contentArea) return false;
        
        return contentArea.scrollTop <= 10; // 10px threshold
    }

    // Function to check if we're at the bottom of quick actions content
    function isAtQuickActionsBottom() {
        const quickActionsSection = document.querySelector('.quick-actions');
        if (!quickActionsSection) return false;
        
        const contentArea = quickActionsSection.querySelector('.scrollable-content');
        if (!contentArea) return false;
        
        return contentArea.scrollHeight - contentArea.scrollTop - contentArea.clientHeight <= 10; // 10px threshold
    }
    
    // 1. Enable section-based scrolling only for screens wider than 1000px
    function isSectionScrollEnabled() {
        return window.innerWidth > 1000;
    }
    
    // Handle wheel events for element-by-element scrolling
    window.addEventListener('wheel', function(event) {
        if (!isSectionScrollEnabled()) return; // Only enable on large screens
        // Check if newspaper overlay is active
        const newspaperOverlay = document.getElementById('newspaperOverlay');
        if (newspaperOverlay && newspaperOverlay.style.display === 'block') {
            // Allow natural scrolling in newspaper overlay
            return;
        }

        // Check if the event originated from a scrollable container
        const scrollableParent = event.target.closest('.scrollable-content, .scrollable-features');
        if (scrollableParent) {
            // Allow natural scrolling within scrollable containers
            return;
        }
        
        // Determine scroll direction
        const scrollDown = event.deltaY > 0;
        
        // Check if we're in a feature card
        if (isInFeatureCard(event)) {
            // If we're at the top of the feature card and scrolling up, allow the event to propagate
            if (!scrollDown && isFeatureCardAtBoundary(event, true)) {
                // Let the event propagate to handle section navigation
                return;
            }
            
            // If we're at the bottom of the feature card and scrolling down, allow the event to propagate
            if (scrollDown && isFeatureCardAtBoundary(event, false)) {
                // Let the event propagate to handle section navigation
                return;
            }
            
            // Otherwise, allow natural scrolling within the feature card
            return;
        }
        
        // Special handling for quick-actions and features sections
        if (isInSpecialSection()) {
            // Only trigger next section when at the bottom and scrolling down
            if (scrollDown && isAtBottomOfSection()) {
                event.preventDefault();
                scrollToNextElement();
            }
            // Otherwise, allow natural scrolling (including scrolling up)
            return;
        }
        
        // Special handling for footer
        if (isInFooter()) {
            if (scrollDown && isAtBottomOfSection()) {
                event.preventDefault();
                scrollToNextElement();
            } else if (!scrollDown && isAtTopOfSection()) {
                event.preventDefault();
                scrollToPreviousElement(); // Go to previous section, not first
            }
            // Allow natural scrolling otherwise
            return;
        }
        
        // Special handling for quick actions section
        if (isInQuickActions()) {
            // When scrolling up and at the top of content, go to previous section
            if (!scrollDown && isAtQuickActionsTop()) {
                event.preventDefault();
                scrollToPreviousElement();
                return;
            }
            
            // When scrolling down and at the bottom of content, go to next section
            if (scrollDown && isAtQuickActionsBottom()) {
                event.preventDefault();
                scrollToNextElement();
                return;
            }
            
            // Allow natural scrolling within the section
            return;
        }
        
        // For other sections, use the original section-by-section scrolling
        event.preventDefault();
        
        // Add a small delay to prevent rapid scrolling
        setTimeout(() => {
            // Scroll to next or previous element based on direction
            if (scrollDown) {
                scrollToNextElement();
            } else {
                // Only allow scrolling up to previous section in quick-actions when in title area
                if (currentSectionIndex > 0 && 
                    navigableElements[currentSectionIndex - 1].classList.contains('quick-actions') && 
                    isInQuickActionsTitle(event)) {
                    scrollToPreviousElement();
                } else if (!navigableElements[currentSectionIndex - 1].classList.contains('quick-actions')) {
                    // For non-quick-actions sections, allow normal up navigation
                    scrollToPreviousElement();
                }
            }
        }, 50);
    }, { passive: false });
    
    // Handle touch events for mobile devices
    let touchStartY = 0;
    
    window.addEventListener('touchstart', function(event) {
        // Check if the event originated from a scrollable container
        const scrollableParent = event.target.closest('.scrollable-content, .scrollable-features');
        if (scrollableParent) {
            // Allow natural touch handling within scrollable containers
            return;
        }
        
        touchStartY = event.touches[0].clientY;
    }, { passive: true });
    
    window.addEventListener('touchmove', function(event) {
        // Check if the event originated from a scrollable container
        const scrollableParent = event.target.closest('.scrollable-content, .scrollable-features');
        const quickActions = event.target.closest('.quick-actions');
        const featuresSection = event.target.closest('.features-section');
        const footer = event.target.closest('.site-footer');
        const featureCard = event.target.closest('.feature-card');
        
        // Allow natural scrolling for Quick Actions, Features section, Footer, and scrollable containers
        if (scrollableParent || quickActions || featuresSection || footer || featureCard) {
            return;
        }
        
        // Prevent default scroll behavior for main page sections
        event.preventDefault();
        
        // Calculate touch direction
        const touchEndY = event.touches[0].clientY;
        const touchDiff = touchStartY - touchEndY;
        
        // Special handling for quick actions section
        if (isInQuickActions()) {
            if (Math.abs(touchDiff) > 50) {
                if (touchDiff > 0 && isAtQuickActionsBottom()) {
                    // Swipe up at bottom - go to next section
                    event.preventDefault();
                    scrollToNextElement();
                } else if (touchDiff < 0 && isAtQuickActionsTop()) {
                    // Swipe down at top - go to previous section
                    event.preventDefault();
                    scrollToPreviousElement();
                }
            }
            return;
        }
        
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
        // Special handling for quick-actions and features sections
        if (isInSpecialSection()) {
            // Only trigger next section when at the bottom and pressing down arrow or space
            if ((event.key === 'ArrowDown' || event.key === ' ') && isAtBottomOfSection()) {
                scrollToNextElement();
            }
            // Allow natural scrolling with arrow up key
            return;
        }
        
        // Special handling for footer
        if (isInFooter()) {
            // Only trigger next section when at the bottom and pressing down arrow or space
            if ((event.key === 'ArrowDown' || event.key === ' ') && isAtBottomOfSection()) {
                scrollToNextElement();
            }
            // Allow natural scrolling with arrow up key
            return;
        }
        
        // Special handling for quick actions section
        if (isInQuickActions()) {
            if (event.key === 'ArrowUp' && isAtQuickActionsTop()) {
                scrollToPreviousElement();
                return;
            }
            
            if ((event.key === 'ArrowDown' || event.key === ' ') && isAtQuickActionsBottom()) {
                scrollToNextElement();
                return;
            }
            
            // Allow natural scrolling within the section
            return;
        }
        
        // Check for arrow down key
        if (event.key === 'ArrowDown') {
            scrollToNextElement();
        }
        
        // Check for arrow up key
        if (event.key === 'ArrowUp') {
            // Only allow scrolling up to previous section in quick-actions when in title area
            if (currentSectionIndex > 0 && 
                navigableElements[currentSectionIndex - 1].classList.contains('quick-actions') && 
                isInQuickActionsTitle(event)) {
                scrollToPreviousElement();
            } else if (!navigableElements[currentSectionIndex - 1].classList.contains('quick-actions')) {
                // For non-quick-actions sections, allow normal up navigation
                scrollToPreviousElement();
            }
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

    // Add scroll event listener to update active dot when manually scrolling
    window.addEventListener('scroll', function() {
        // Find which section is currently most visible in the viewport
        let maxVisibility = 0;
        let mostVisibleIndex = currentSectionIndex;
        
        navigableElements.forEach((element, index) => {
            const rect = element.getBoundingClientRect();
            const windowHeight = window.innerHeight;
            
            // Calculate how much of the element is visible in the viewport
            const visibleHeight = Math.min(rect.bottom, windowHeight) - Math.max(rect.top, 0);
            const visibility = visibleHeight > 0 ? visibleHeight / element.offsetHeight : 0;
            
            // Update the most visible section if this one is more visible
            if (visibility > maxVisibility) {
                maxVisibility = visibility;
                mostVisibleIndex = index;
            }
        });
        
        // Only update if we've found a different section to be most visible
        if (mostVisibleIndex !== currentSectionIndex) {
            currentSectionIndex = mostVisibleIndex;
            updateActiveDot();
        }
    }, { passive: true });

    // Intersection Observer for section animations
    const observerOptions = {
        root: null,
        threshold: 0.1,
        rootMargin: '0px'
    };

    const sectionObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            // Ignore if the intersection is from scrolling within a scrollable container or Quick Actions
            if (entry.target.closest('.scrollable-content, .scrollable-features, .quick-actions')) {
                return;
            }
            
            if (entry.isIntersecting) {
                entry.target.classList.remove('section-hidden');
            }
        });
    }, observerOptions);

    // Observe all sections except the content inside scrollable containers and Quick Actions
    document.querySelectorAll('section').forEach(section => {
        if (!section.closest('.scrollable-content, .scrollable-features, .quick-actions')) {
            sectionObserver.observe(section);
        }
    });

    // Prevent scroll events from bubbling up from scrollable containers
    document.querySelectorAll('.scrollable-content, .scrollable-features, .feature-card').forEach(container => {
        container.addEventListener('scroll', (e) => {
            e.stopPropagation();
        });
    });
    
    // Add scroll event listener to detect when we reach the bottom of special sections
    window.addEventListener('scroll', function() {
        // Only check if we're in a special section
        if (isInSpecialSection() && isAtBottomOfSection()) {
            // Add a class to indicate we're at the bottom
            navigableElements[currentSectionIndex].classList.add('at-bottom');
        } else {
            // Remove the class if we're not at the bottom
            navigableElements[currentSectionIndex].classList.remove('at-bottom');
        }
    }, { passive: true });
}); 