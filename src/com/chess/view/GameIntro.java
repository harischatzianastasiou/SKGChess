package com.chess.view;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;

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
            private final int ANIMATION_DURATION = 1500; // 1.5 seconds fade in
            private final int STAY_DURATION = 2000; // 2 seconds stay
            private final int TIMER_DELAY = 20;
            private long startTime;
            private float towerAnimationPhase = 0f;

            {
                setBackground(Color.BLACK);
                startTime = System.currentTimeMillis();
                fadeTimer = new Timer(TIMER_DELAY, e -> {
                    long elapsed = System.currentTimeMillis() - startTime;
                    
                    // Fade in phase
                    if (elapsed <= ANIMATION_DURATION) {
                        alpha = Math.min(1f, (float) elapsed / ANIMATION_DURATION);
                    } 
                    // Stay phase
                    else if (elapsed <= ANIMATION_DURATION + STAY_DURATION) {
                        alpha = 1.0f;
                    }
                    // End animation
                    else {
                        fadeTimer.stop();
                        introFinished = true;
                        introDialog.dispose();
                    }
                    
                    towerAnimationPhase = (elapsed % 2000) / 2000f;
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

                // Draw background elements first
                drawBackground(g2d);

                // Set composite for fade effect
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

                // Draw logo text
                Font logoFont = new Font("Palatino Linotype", Font.BOLD, 72);
                g2d.setFont(logoFont);
                String text = "SKGChess";
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(text);
                int textHeight = fm.getHeight();
                int x = (getWidth() - textWidth) / 2;
                int y = (getHeight() - textHeight) / 2 + fm.getAscent();

                // Draw text shadow
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.drawString(text, x + 3, y + 3);

                // Draw main text with gradient
                GradientPaint gradient = new GradientPaint(
                    x, y - textHeight,
                    new Color(255, 255, 255),
                    x, y,
                    new Color(200, 200, 200)
                );
                g2d.setPaint(gradient);
                g2d.drawString(text, x, y);

                // Draw White Tower silhouette
                int towerSize = 160;
                int towerX = (getWidth() - towerSize) / 2;
                int towerY = y + 20;
                drawWhiteTower(g2d, towerX, towerY, towerSize);
            }

            private void drawBackground(Graphics2D g2d) {
                // Simple black background
                g2d.setColor(Color.BLACK);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Draw Toumba Stadium silhouette
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.15f));
                g2d.setColor(new Color(255, 255, 255));
                
                // Stadium outline
                int stadiumWidth = getWidth() * 2/3;
                int stadiumHeight = getHeight() / 3;
                int stadiumX = (getWidth() - stadiumWidth) / 2;
                int stadiumY = getHeight() - stadiumHeight - 50;

                // Draw stadium arcs
                g2d.setStroke(new BasicStroke(2));
                g2d.drawArc(stadiumX, stadiumY, stadiumWidth, stadiumHeight * 2, 0, 180);
                g2d.drawLine(stadiumX, stadiumY + stadiumHeight, stadiumX + stadiumWidth, stadiumY + stadiumHeight);

                // Draw stadium details (floodlights)
                int lightCount = 4;
                for (int i = 0; i < lightCount; i++) {
                    int x = stadiumX + (stadiumWidth * (i + 1))/(lightCount + 1);
                    g2d.drawLine(x, stadiumY + stadiumHeight, x, stadiumY + stadiumHeight - 40);
                    // Light glow
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.05f));
                    g2d.fillOval(x - 15, stadiumY + stadiumHeight - 55, 30, 30);
                }
            }

            private void drawWhiteTower(Graphics2D g2d, int x, int y, int size) {
                // Save original composite
                Composite originalComposite = g2d.getComposite();
                
                // Create a gradient for the tower's stone texture
                GradientPaint towerGradient = new GradientPaint(
                    x, y,
                    new Color(238, 232, 205), // Authentic White Tower color
                    x, y + size,
                    new Color(215, 210, 190)  // Slightly darker authentic tone
                );
                g2d.setPaint(towerGradient);
                g2d.setStroke(new BasicStroke(2));

                // Draw main cylindrical tower body
                int bodyWidth = (int)(size * 0.4); // Thinner, more authentic proportion
                int bodyHeight = (int)(size * 0.85); // Taller, like the real tower
                int bodyX = x + (size - bodyWidth) / 2;
                int bodyY = y + size - bodyHeight;

                // Draw the cylindrical body
                g2d.fillRect(bodyX, bodyY, bodyWidth, bodyHeight);

                // Draw the characteristic top section (wider than body)
                int topWidth = (int)(bodyWidth * 1.3);
                int topHeight = (int)(bodyHeight * 0.15);
                int topX = bodyX + (bodyWidth - topWidth) / 2;
                int topY = bodyY;
                
                g2d.setColor(new Color(228, 222, 195));
                g2d.fillRect(topX, topY, topWidth, topHeight);

                // Draw the crenellations (more accurate count and shape)
                int creCount = 12; // Actual number of crenellations
                int creWidth = topWidth / creCount;
                int creHeight = (int)(topHeight * 0.4);
                g2d.setColor(new Color(218, 212, 185));
                for (int i = 0; i < creCount; i++) {
                    g2d.fillRect(
                        topX + (i * creWidth),
                        topY - creHeight,
                        creWidth - 2,
                        creHeight
                    );
                }

                // Draw the characteristic horizontal bands
                g2d.setColor(new Color(208, 202, 175));
                int bandCount = 6;
                int bandSpacing = bodyHeight / (bandCount + 1);
                for (int i = 1; i <= bandCount; i++) {
                    g2d.fillRect(
                        bodyX - 4,
                        bodyY + (i * bandSpacing),
                        bodyWidth + 8,
                        4
                    );
                }

                // Draw the authentic arched windows (3 rows, 2 per row)
                g2d.setColor(new Color(40, 40, 40));
                int windowRows = 3;
                int windowsPerRow = 2;
                int windowWidth = bodyWidth / 5;
                int windowHeight = bandSpacing / 2;
                
                for (int row = 0; row < windowRows; row++) {
                    int windowY = bodyY + topHeight + (row * bandSpacing * 2) + bandSpacing/2;
                    for (int col = 0; col < windowsPerRow; col++) {
                        int windowX = bodyX + (col * (bodyWidth - windowWidth)) + bodyWidth/4;
                        
                        // Draw arched window
                        g2d.fillRoundRect(
                            windowX,
                            windowY,
                            windowWidth,
                            windowHeight,
                            windowWidth/2,
                            windowHeight/2
                        );
                    }
                }

                // Draw base
                g2d.setColor(new Color(198, 192, 165));
                int baseWidth = (int)(bodyWidth * 1.4);
                int baseHeight = (int)(size * 0.08);
                g2d.fillRect(
                    bodyX + (bodyWidth - baseWidth) / 2,
                    bodyY + bodyHeight - baseHeight,
                    baseWidth,
                    baseHeight
                );

                // Add subtle stone texture
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
                for (int i = 0; i < bodyHeight; i += 4) {
                    g2d.setColor(new Color(0, 0, 0, 10));
                    g2d.drawLine(bodyX, bodyY + i, bodyX + bodyWidth, bodyY + i);
                }

                // Restore original composite
                g2d.setComposite(originalComposite);
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