// Music Player Class for authenticated users
class MusicPlayer {
    constructor() {
        // Initialize audio element
        this.audio = new Audio('/music/chess-theme.mp3'); // Make sure to add your music file
        this.audio.loop = true; // Loop the music
        
        // Get DOM elements
        this.playPauseBtn = document.querySelector('.music-player-btn.play-pause');
        this.prevBtn = document.querySelector('.music-player-btn.prev');
        this.nextBtn = document.querySelector('.music-player-btn.next');
        this.progressBar = document.querySelector('.music-player-progress-bar');
        this.progressContainer = document.querySelector('.music-player-progress');
        this.currentTimeEl = document.querySelector('.current-time');
        this.totalTimeEl = document.querySelector('.total-time');
        
        // Only initialize if elements exist (authenticated user)
        if (this.playPauseBtn) {
            // Bind event listeners
            this.playPauseBtn.addEventListener('click', () => this.togglePlay());
            this.progressContainer.addEventListener('click', (e) => this.setProgress(e));
            this.audio.addEventListener('timeupdate', () => this.updateProgress());
            this.audio.addEventListener('loadedmetadata', () => this.updateTotalTime());
            
            // Initialize state
            this.isPlaying = false;
        }
    }
    
    // Toggle play/pause
    togglePlay() {
        if (this.isPlaying) {
            this.pause();
        } else {
            this.play();
        }
    }
    
    // Play music
    play() {
        this.isPlaying = true;
        this.playPauseBtn.innerHTML = '<i class="fas fa-pause"></i>';
        this.audio.play();
    }
    
    // Pause music
    pause() {
        this.isPlaying = false;
        this.playPauseBtn.innerHTML = '<i class="fas fa-play"></i>';
        this.audio.pause();
    }
    
    // Update progress bar
    updateProgress() {
        const { duration, currentTime } = this.audio;
        const progressPercent = (currentTime / duration) * 100;
        this.progressBar.style.width = `${progressPercent}%`;
        this.currentTimeEl.textContent = this.formatTime(currentTime);
    }
    
    // Set progress bar position
    setProgress(e) {
        const width = this.progressContainer.clientWidth;
        const clickX = e.offsetX;
        const duration = this.audio.duration;
        this.audio.currentTime = (clickX / width) * duration;
    }
    
    // Update total time display
    updateTotalTime() {
        this.totalTimeEl.textContent = this.formatTime(this.audio.duration);
    }
    
    // Format time in minutes and seconds
    formatTime(seconds) {
        const minutes = Math.floor(seconds / 60);
        const secs = Math.floor(seconds % 60);
        return `${minutes}:${secs.toString().padStart(2, '0')}`;
    }
}

// Music Player Button Handler
document.addEventListener('DOMContentLoaded', () => {
    const musicButton = document.querySelector('.music-player');
    const musicIcon = document.getElementById('musicIcon');
    let isPlaying = false;
    
    if (musicButton && musicIcon) {
        musicButton.addEventListener('click', () => {
            isPlaying = !isPlaying;
            musicIcon.className = isPlaying ? 'fas fa-volume-mute' : 'fas fa-volume-up';
            // Add your music toggle logic here
        });
    }
}); 