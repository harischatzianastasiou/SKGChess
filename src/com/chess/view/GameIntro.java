package com.chess.view;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.Timer;

public class GameIntro {
    private final Dimension WINDOW_SIZE = new Dimension(743, 655);
    private boolean introFinished = false;
    private JDialog introDialog;

    public GameIntro() {
        showIntroScreen();
    }

    private void showIntroScreen() {
        introDialog = new JDialog((Frame) null, "SKGChess", true);
        introDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        introDialog.setSize(WINDOW_SIZE);
        introDialog.setLocationRelativeTo(null);

        // Create custom panel for logo animation
        JPanel logoPanel = new JPanel() {
            private float alpha = 0f;
            private Timer fadeTimer;
            private final int ANIMATION_DURATION = 2000; // 2 seconds
            private final int TIMER_DELAY = 20;
            private long startTime;

            {
                setBackground(Color.BLACK);
                startTime = System.currentTimeMillis();
                fadeTimer = new Timer(TIMER_DELAY, e -> {
                    long elapsed = System.currentTimeMillis() - startTime;
                    alpha = Math.min(1f, (float) elapsed / ANIMATION_DURATION);
                    
                    if (elapsed >= ANIMATION_DURATION + 500) { // Wait extra 500ms at full opacity
                        fadeTimer.stop();
                        introFinished = true;
                        introDialog.dispose();
                    }
                    repaint();
                });
                fadeTimer.start();
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // Set composite for fade effect
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

                // Draw logo text
                g2d.setColor(Color.WHITE);
                Font logoFont = new Font("Palatino Linotype", Font.BOLD, 72);
                g2d.setFont(logoFont);
                String text = "SKGChess";
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(text);
                int textHeight = fm.getHeight();
                int x = (getWidth() - textWidth) / 2;
                int y = (getHeight() - textHeight) / 2 + fm.getAscent();
                g2d.drawString(text, x, y);

                // Draw decorative chess piece silhouette (knight)
                int pieceSize = 100;
                int pieceX = (getWidth() - pieceSize) / 2;
                int pieceY = y + 20;
                drawKnightSilhouette(g2d, pieceX, pieceY, pieceSize);
            }

            private void drawKnightSilhouette(Graphics2D g2d, int x, int y, int size) {
                Path2D path = new Path2D.Float();
                // Simplified knight silhouette
                path.moveTo(x + size * 0.2, y + size * 0.8);
                path.curveTo(x + size * 0.2, y + size * 0.6,
                           x + size * 0.3, y + size * 0.4,
                           x + size * 0.4, y + size * 0.3);
                path.curveTo(x + size * 0.5, y + size * 0.2,
                           x + size * 0.6, y + size * 0.1,
                           x + size * 0.7, y + size * 0.2);
                path.lineTo(x + size * 0.8, y + size * 0.8);
                path.closePath();

                g2d.fill(path);
            }
        };

        introDialog.add(logoPanel);
        introDialog.setVisible(true);
    }

    public boolean isIntroFinished() {
        return introFinished;
    }

    public Point getLocation() {
        return introDialog.getLocation();
    }
} 