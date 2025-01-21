package com.chess.view;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import com.chess.model.Alliance;

public class GameModeSelector {
    private boolean isHumanVsHuman;
    private Alliance playerAlliance;
    private boolean selectionMade;
    private JDialog dialog;
    private Point location;
    private final Dimension WINDOW_SIZE = new Dimension(743, 655);
    private final Color BACKGROUND_COLOR = new Color(45, 45, 45);
    private final Color BUTTON_COLOR = new Color(60, 60, 60);
    private final Color TEXT_COLOR = Color.WHITE;
    private final Font TITLE_FONT = new Font("Palatino Linotype", Font.BOLD, 36);
    private final Font BUTTON_FONT = new Font("Segoe UI", Font.PLAIN, 16);

    public GameModeSelector(Point location) {
        this.location = location;
        selectionMade = false;
        showGameModeDialog();
    }

    private void showGameModeDialog() {
        dialog = new JDialog((Frame) null, "SKGChess", true);
        dialog.setSize(WINDOW_SIZE);
        dialog.setLayout(new BorderLayout());
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        if (location != null) {
            dialog.setLocation(location);
        } else {
            dialog.setLocationRelativeTo(null);
        }

        // Create main panel with dark background
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(50, 50, 50, 50));

        // Add title
        JLabel titleLabel = new JLabel("Game Mode Selection", SwingConstants.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setBorder(new EmptyBorder(0, 0, 30, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Create options panel
        JPanel optionsPanel = new JPanel(new GridLayout(4, 1, 20, 20));
        optionsPanel.setBackground(BACKGROUND_COLOR);

        // Mode selection
        ButtonGroup modeGroup = new ButtonGroup();
        JRadioButton humanVsHuman = createStyledRadioButton("Human vs Human", true);
        JRadioButton humanVsAI = createStyledRadioButton("Human vs AI", false);
        modeGroup.add(humanVsHuman);
        modeGroup.add(humanVsAI);

        // Color selection
        ButtonGroup colorGroup = new ButtonGroup();
        JRadioButton whiteButton = createStyledRadioButton("Play as White", true);
        JRadioButton blackButton = createStyledRadioButton("Play as Black", false);
        colorGroup.add(whiteButton);
        colorGroup.add(blackButton);

        // Create panels for radio buttons with titles
        JPanel modePanel = createStyledPanel("Select Game Mode");
        modePanel.add(humanVsHuman);
        modePanel.add(humanVsAI);

        JPanel colorPanel = createStyledPanel("Select Color");
        colorPanel.add(whiteButton);
        colorPanel.add(blackButton);
        colorPanel.setVisible(false);

        optionsPanel.add(modePanel);
        optionsPanel.add(colorPanel);

        // Show/hide color selection based on game mode
        humanVsAI.addActionListener(e -> colorPanel.setVisible(true));
        humanVsHuman.addActionListener(e -> colorPanel.setVisible(false));

        // Create start button
        JButton startButton = new JButton("Start Game");
        styleButton(startButton);
        startButton.addActionListener(e -> {
            isHumanVsHuman = humanVsHuman.isSelected();
            playerAlliance = whiteButton.isSelected() ? Alliance.WHITE : Alliance.BLACK;
            selectionMade = true;
            dialog.dispose();
        });

        // Add components to main panel
        JPanel centerPanel = new JPanel(new BorderLayout(20, 20));
        centerPanel.setBackground(BACKGROUND_COLOR);
        centerPanel.add(optionsPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.add(startButton);
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        dialog.add(mainPanel);

        dialog.setVisible(true);
    }

    private JRadioButton createStyledRadioButton(String text, boolean selected) {
        JRadioButton button = new JRadioButton(text);
        button.setFont(BUTTON_FONT);
        button.setForeground(TEXT_COLOR);
        button.setBackground(BACKGROUND_COLOR);
        button.setSelected(selected);
        button.setFocusPainted(false);
        return button;
    }

    private JPanel createStyledPanel(String title) {
        JPanel panel = new JPanel(new GridLayout(0, 1, 10, 10));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(TEXT_COLOR),
            title,
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            BUTTON_FONT,
            TEXT_COLOR
        ));
        return panel;
    }

    private void styleButton(JButton button) {
        button.setFont(BUTTON_FONT);
        button.setBackground(BUTTON_COLOR);
        button.setForeground(TEXT_COLOR);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(200, 40));
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(80, 80, 80));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(BUTTON_COLOR);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(40, 40, 40));
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(80, 80, 80));
            }
        });
    }

    public boolean isHumanVsHuman() {
        return isHumanVsHuman;
    }

    public Alliance getPlayerAlliance() {
        return playerAlliance;
    }

    public boolean isSelectionMade() {
        return selectionMade;
    }

    public Point getLocation() {
        return dialog.getLocation();
    }
} 