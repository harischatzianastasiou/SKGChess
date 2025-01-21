package com.chess.view;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.chess.model.Alliance;

public class GameModeSelector {
    private boolean isHumanVsHuman;
    private Alliance playerAlliance;
    private boolean selectionMade;

    public GameModeSelector() {
        selectionMade = false;
        showGameModeDialog();
    }

    private void showGameModeDialog() {
        JDialog dialog = new JDialog((Frame) null, "Game Mode Selection", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel modePanel = new JPanel(new GridLayout(2, 1, 5, 5));
        ButtonGroup modeGroup = new ButtonGroup();
        JRadioButton humanVsHuman = new JRadioButton("Human vs Human");
        JRadioButton humanVsAI = new JRadioButton("Human vs AI");
        modeGroup.add(humanVsHuman);
        modeGroup.add(humanVsAI);
        humanVsHuman.setSelected(true);
        modePanel.add(humanVsHuman);
        modePanel.add(humanVsAI);
        modePanel.setBorder(BorderFactory.createTitledBorder("Select Game Mode"));

        JPanel colorPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        ButtonGroup colorGroup = new ButtonGroup();
        JRadioButton whiteButton = new JRadioButton("Play as White");
        JRadioButton blackButton = new JRadioButton("Play as Black");
        colorGroup.add(whiteButton);
        colorGroup.add(blackButton);
        whiteButton.setSelected(true);
        colorPanel.add(whiteButton);
        colorPanel.add(blackButton);
        colorPanel.setBorder(BorderFactory.createTitledBorder("Select Color"));
        colorPanel.setVisible(false);

        // Show/hide color selection based on game mode
        humanVsAI.addActionListener(e -> colorPanel.setVisible(true));
        humanVsHuman.addActionListener(e -> colorPanel.setVisible(false));

        JButton startButton = new JButton("Start Game");
        startButton.addActionListener(e -> {
            isHumanVsHuman = humanVsHuman.isSelected();
            playerAlliance = whiteButton.isSelected() ? Alliance.WHITE : Alliance.BLACK;
            selectionMade = true;
            dialog.dispose();
        });

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(modePanel);
        mainPanel.add(colorPanel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(startButton);

        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
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
} 