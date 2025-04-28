// Theme switcher functionality
document.addEventListener('DOMContentLoaded', function() {
    // Get all theme switchers
    const themeSwitchers = document.querySelectorAll('.theme-switcher');
    
    // Available themes
    const themes = {
        'purple': 'board-purple.css',
        'green': 'board-green.css',
        'brown': 'board-brown.css',
        'gold': 'board-gold.css',
        'ocean': 'board-ocean.css'
    };
    
    // Function to apply theme
    function applyTheme(themeName) {
        // Remove all theme stylesheets
        document.querySelectorAll('link[href*="board-"]').forEach(link => {
            link.disabled = true;
        });
        
        // Enable the selected theme stylesheet
        const themeLink = document.querySelector(`link[href*="${themes[themeName]}"]`);
        if (themeLink) {
            themeLink.disabled = false;
        }
        
        // Update board class
        const chessBoard = document.querySelector('.chess-board');
        if (chessBoard) {
            chessBoard.className = 'chess-board';
            chessBoard.classList.add(themeName + '-theme');
        }
        
        // Update theme switcher active state
        themeSwitchers.forEach(switcher => {
            switcher.classList.remove('active');
            if (switcher.classList.contains(themeName + '-theme')) {
                switcher.classList.add('active');
            }
        });
    }
    
    // Add click event to each theme switcher
    themeSwitchers.forEach(switcher => {
        switcher.addEventListener('click', function() {
            // Get theme name from class (e.g., 'purple-theme' -> 'purple')
            const themeClass = Array.from(this.classList).find(cls => cls.endsWith('-theme'));
            if (themeClass) {
                const themeName = themeClass.replace('-theme', '');
                applyTheme(themeName);
                
                // Save theme preference
                localStorage.setItem('chess-theme', themeName);
            }
        });
    });
    
    // Load saved theme preference
    const savedTheme = localStorage.getItem('chess-theme');
    if (savedTheme && themes[savedTheme]) {
        applyTheme(savedTheme);
    } else {
        // Default to purple theme
        applyTheme('purple');
    }
}); 