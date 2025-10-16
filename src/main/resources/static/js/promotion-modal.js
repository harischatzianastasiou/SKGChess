/**
 * Promotion Modal - Handles piece selection during pawn promotion
 * This modal appears when a pawn reaches the promotion rank and allows the player
 * to select which piece to promote to (Queen, Rook, Bishop, or Knight)
 */
class PromotionModal {
    constructor() {
        this.modal = null; // Reference to the modal element
        this.selectedPiece = null; // Store the selected promotion piece
        this.resolvePromise = null; // Promise resolver for piece selection
        this.isVisible = false; // Track modal visibility state
    }

    /**
     * Show the promotion modal and wait for user selection
     * @param {string} playerAlliance - The alliance of the promoting player (WHITE or BLACK)
     * @returns {Promise<string>} - Promise that resolves to the selected piece type
     */
    async showPromotionModal(playerAlliance) {
        // Return a promise that resolves when user selects a piece
        return new Promise((resolve) => {
            this.resolvePromise = resolve;
            this.createModal(playerAlliance);
            this.showModal();
        });
    }

    /**
     * Create the promotion modal HTML structure
     * @param {string} playerAlliance - The alliance of the promoting player
     */
    createModal(playerAlliance) {
        // Remove existing modal if it exists
        this.hideModal();

        // Create modal container
        this.modal = document.createElement('div');
        this.modal.className = 'promotion-modal-overlay';
        this.modal.innerHTML = `
            <div class="promotion-modal">
                <div class="promotion-modal-header">
                    <h3>Choose Promotion Piece</h3>
                    <p>Select which piece to promote your pawn to:</p>
                </div>
                <div class="promotion-pieces-container">
                    <div class="promotion-piece-option" data-piece="QUEEN">
                        <div class="promotion-piece-icon ${playerAlliance.toLowerCase()}-queen"></div>
                        <span>Queen</span>
                    </div>
                    <div class="promotion-piece-option" data-piece="ROOK">
                        <div class="promotion-piece-icon ${playerAlliance.toLowerCase()}-rook"></div>
                        <span>Rook</span>
                    </div>
                    <div class="promotion-piece-option" data-piece="BISHOP">
                        <div class="promotion-piece-icon ${playerAlliance.toLowerCase()}-bishop"></div>
                        <span>Bishop</span>
                    </div>
                    <div class="promotion-piece-option" data-piece="KNIGHT">
                        <div class="promotion-piece-icon ${playerAlliance.toLowerCase()}-knight"></div>
                        <span>Knight</span>
                    </div>
                </div>
            </div>
        `;

        // Add event listeners to piece options
        const pieceOptions = this.modal.querySelectorAll('.promotion-piece-option');
        pieceOptions.forEach(option => {
            option.addEventListener('click', () => {
                const selectedPiece = option.dataset.piece;
                this.selectPiece(selectedPiece);
            });

            // Add hover effects for better UX
            option.addEventListener('mouseenter', () => {
                option.classList.add('hover');
            });

            option.addEventListener('mouseleave', () => {
                option.classList.remove('hover');
            });
        });

        // Add modal to document
        document.body.appendChild(this.modal);
    }

    /**
     * Show the modal with animation
     */
    showModal() {
        if (!this.modal) return;

        this.isVisible = true;
        this.modal.style.display = 'flex';
        
        // Add animation classes
        requestAnimationFrame(() => {
            this.modal.classList.add('show');
        });

        // Prevent body scrolling when modal is open
        document.body.style.overflow = 'hidden';
    }

    /**
     * Hide the modal with animation
     */
    hideModal() {
        if (!this.modal || !this.isVisible) return;

        this.isVisible = false;
        this.modal.classList.remove('show');
        
        // Remove modal after animation
        setTimeout(() => {
            if (this.modal && this.modal.parentNode) {
                this.modal.parentNode.removeChild(this.modal);
            }
            this.modal = null;
        }, 300);

        // Restore body scrolling
        document.body.style.overflow = '';
    }

    /**
     * Handle piece selection
     * @param {string} pieceType - The selected piece type
     */
    selectPiece(pieceType) {
        this.selectedPiece = pieceType;
        
        // Add visual feedback for selection
        const selectedOption = this.modal.querySelector(`[data-piece="${pieceType}"]`);
        if (selectedOption) {
            selectedOption.classList.add('selected');
        }

        // Resolve the promise with the selected piece
        if (this.resolvePromise) {
            this.resolvePromise(pieceType);
            this.resolvePromise = null;
        }

        // Hide modal after a short delay to show selection
        setTimeout(() => {
            this.hideModal();
        }, 200);
    }

    /**
     * Check if modal is currently visible
     * @returns {boolean} - True if modal is visible
     */
    isModalVisible() {
        return this.isVisible;
    }
}

// Create global instance for use throughout the application
window.promotionModal = new PromotionModal();
