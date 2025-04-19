// Initialize Chess.com Web API
const chessAPI = new ChessWebApi();

// Fetch and display chess news
async function fetchChessNews() {
    try {
        // Get news container element
        const newsContainer = document.getElementById('chessNews');
        if (!newsContainer) return;

        // Show loading state
        newsContainer.innerHTML = Array(3).fill(0).map(() => `
            <div class="news-card loading">
                <div class="news-image">
                    <img src="/images/default-news.jpg" alt="Loading..." class="loading">
                </div>
                <div class="news-content">
                    <h3>Loading...</h3>
                    <p>Loading content...</p>
                </div>
            </div>
        `).join('');

        // Fetch news from Chess.com API
        const response = await chessAPI.getNews();
        const news = response.body;

        // Clear loading state
        newsContainer.innerHTML = '';

        // Display news items
        news.slice(0, 6).forEach(item => {
            const newsCard = document.createElement('div');
            newsCard.className = 'news-card';
            
            // Create image element with loading state
            const img = new Image();
            img.className = 'loading';
            img.onload = () => img.classList.remove('loading');
            img.onerror = () => {
                img.src = '/images/default-news.jpg';
                img.alt = 'Default news image';
            };
            img.src = item.image || '/images/default-news.jpg';
            img.alt = item.title;

            newsCard.innerHTML = `
                <div class="news-image"></div>
                <div class="news-content">
                    <h3>${item.title}</h3>
                    <p>${item.excerpt}</p>
                    <a href="${item.url}" target="_blank" class="read-more">
                        Read More
                        <i class="fas fa-arrow-right"></i>
                    </a>
                </div>
            `;
            
            // Add the image to the news-image div
            newsCard.querySelector('.news-image').appendChild(img);
            newsContainer.appendChild(newsCard);
        });
    } catch (error) {
        console.error('Error fetching chess news:', error);
        displayFallbackNews();
    }
}

// Display fallback news if API fails
function displayFallbackNews() {
    const newsContainer = document.getElementById('chessNews');
    if (!newsContainer) return;

    const fallbackNews = [
        {
            title: 'Latest Tournament Updates',
            excerpt: 'Stay updated with the latest chess tournament results and upcoming events in Thessaloniki.',
            image: '/images/tournament.jpg'
        },
        {
            title: 'Chess Strategy Tips',
            excerpt: 'Improve your game with expert advice and strategic insights from local chess masters.',
            image: '/images/strategy.jpg'
        },
        {
            title: 'Community Spotlight',
            excerpt: 'Featured stories and achievements from our vibrant chess community in Thessaloniki.',
            image: '/images/community.jpg'
        }
    ];

    newsContainer.innerHTML = '';
    fallbackNews.forEach(item => {
        const newsCard = document.createElement('div');
        newsCard.className = 'news-card';
        
        // Create image element with loading state
        const img = new Image();
        img.className = 'loading';
        img.onload = () => img.classList.remove('loading');
        img.onerror = () => {
            img.src = '/images/default-news.jpg';
            img.alt = 'Default news image';
        };
        img.src = item.image;
        img.alt = item.title;

        newsCard.innerHTML = `
            <div class="news-image"></div>
            <div class="news-content">
                <h3>${item.title}</h3>
                <p>${item.excerpt}</p>
                <a href="#" class="read-more">
                    Read More
                    <i class="fas fa-arrow-right"></i>
                </a>
            </div>
        `;
        
        // Add the image to the news-image div
        newsCard.querySelector('.news-image').appendChild(img);
        newsContainer.appendChild(newsCard);
    });
}

// Handle My Games section
function initializeMyGames() {
    const recentGames = document.getElementById('recentGames');
    if (!recentGames) return;

    // Fetch user's recent games (to be implemented with your backend)
    fetchUserGames()
        .then(games => displayRecentGames(games))
        .catch(error => {
            console.error('Error fetching user games:', error);
            displayPlaceholderGames();
        });
}

// Display placeholder games until backend is implemented
function displayPlaceholderGames() {
    const recentGames = document.getElementById('recentGames');
    if (!recentGames) return;

    const placeholderGames = [
        { opponent: 'Player1', result: 'Win', date: '2024-03-15', rating: 1250 },
        { opponent: 'Player2', result: 'Loss', date: '2024-03-14', rating: 1245 },
        { opponent: 'Player3', result: 'Draw', date: '2024-03-13', rating: 1248 }
    ];

    placeholderGames.forEach(game => {
        const gameCard = document.createElement('div');
        gameCard.className = `game-card ${game.result.toLowerCase()}`;
        gameCard.innerHTML = `
            <div class="game-info">
                <span class="opponent">vs ${game.opponent}</span>
                <span class="result ${game.result.toLowerCase()}">${game.result}</span>
            </div>
            <div class="game-details">
                <span class="date">${game.date}</span>
                <span class="rating">Rating: ${game.rating}</span>
            </div>
        `;
        recentGames.appendChild(gameCard);
    });
}

// Initialize features when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    fetchChessNews();
    initializeMyGames();
}); 