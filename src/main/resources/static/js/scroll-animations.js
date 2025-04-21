/**
 * Scroll Animations JavaScript
 * Handles smooth scrolling, section transitions, and scroll indicator functionality
 */

// Wait for the DOM to be fully loaded
document.addEventListener('DOMContentLoaded', function() {
    // Get all sections on the page
    const sections = document.querySelectorAll('section');
    
    // Get the scroll indicator element
    const scrollIndicator = document.querySelector('.scroll-indicator');
    
    // Initialize the first section as visible
    if (sections.length > 0) {
        sections[0].classList.add('section-visible');
    }
    
    // Function to scroll to a specific section
    function scrollToSection(section) {
        // Smooth scroll to the section
        section.scrollIntoView({ behavior: 'smooth' });
        
        // Add visible class to the section after scrolling
        setTimeout(() => {
            section.classList.add('section-visible');
        }, 300);
    }
    
    // Add click event to the scroll indicator
    if (scrollIndicator) {
        scrollIndicator.addEventListener('click', function() {
            // Find the next section after the hero section
            const heroSection = document.querySelector('.hero-section');
            if (heroSection && sections.length > 1) {
                // Get the next section (index 1)
                const nextSection = sections[1];
                scrollToSection(nextSection);
            }
        });
    }
    
    // Handle scroll events to show/hide sections based on visibility
    window.addEventListener('scroll', function() {
        // Get the current scroll position
        const scrollPosition = window.scrollY;
        
        // Check each section to see if it's in the viewport
        sections.forEach((section, index) => {
            // Get the section's position and dimensions
            const sectionTop = section.offsetTop - 100; // Offset for header
            const sectionHeight = section.offsetHeight;
            
            // Check if the section is in the viewport
            if (scrollPosition >= sectionTop && scrollPosition < sectionTop + sectionHeight) {
                // Add visible class to the current section
                section.classList.add('section-visible');
                
                // Hide the scroll indicator when not at the top
                if (scrollIndicator && index > 0) {
                    scrollIndicator.style.opacity = '0';
                } else if (scrollIndicator) {
                    scrollIndicator.style.opacity = '1';
                }
            } else {
                // Remove visible class from sections not in the viewport
                section.classList.remove('section-visible');
            }
        });
    });
    
    // Add keyboard navigation for accessibility
    document.addEventListener('keydown', function(event) {
        // Check for arrow down key
        if (event.key === 'ArrowDown') {
            // Find the current visible section
            const currentSection = Array.from(sections).find(section => 
                section.classList.contains('section-visible')
            );
            
            if (currentSection) {
                // Find the next section
                const currentIndex = Array.from(sections).indexOf(currentSection);
                if (currentIndex < sections.length - 1) {
                    // Scroll to the next section
                    scrollToSection(sections[currentIndex + 1]);
                }
            }
        }
        
        // Check for arrow up key
        if (event.key === 'ArrowUp') {
            // Find the current visible section
            const currentSection = Array.from(sections).find(section => 
                section.classList.contains('section-visible')
            );
            
            if (currentSection) {
                // Find the previous section
                const currentIndex = Array.from(sections).indexOf(currentSection);
                if (currentIndex > 0) {
                    // Scroll to the previous section
                    scrollToSection(sections[currentIndex - 1]);
                }
            }
        }
    });
}); 