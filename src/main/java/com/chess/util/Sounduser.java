package com.chess.util;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Sounduser {
    private static final String MOVE_SOUND_PATH = "sounds/Move.wav";
    private static final String CAPTURE_SOUND_PATH = "sounds/Capture.wav";
    private static final String CHECK_SOUND_PATH = "sounds/Check.wav";
    private static final String CHECKMATE_SOUND_PATH = "sounds/Checkmate.mp3";
    
    private static void playSound(String soundPath) {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(soundPath));
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }
    
    public static void playMoveSound() {
        playSound(MOVE_SOUND_PATH);
    }
    
    public static void playCaptureSound() {
        playSound(CAPTURE_SOUND_PATH);
    }

    public static void playCheckSound() {
        playSound(CHECK_SOUND_PATH);
    }

    public static void playCheckmateSound() {
        playSound(CHECKMATE_SOUND_PATH);
    }
} 