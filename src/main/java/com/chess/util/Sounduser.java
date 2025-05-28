package com.chess.util;

import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class Sounduser {
    private static final String MOVE_SOUND_PATH = "static/sounds/Move.wav";
    private static final String CAPTURE_SOUND_PATH = "static/sounds/Capture.wav";
    private static final String CHECK_SOUND_PATH = "static/sounds/Check.wav";
    private static final String CHECKMATE_SOUND_PATH = "static/sounds/Checkmate.mp3";
    
    private static void playSound(String soundPath) {
        try {
            // Load the sound file from classpath
            Resource resource = new ClassPathResource(soundPath);
            InputStream inputStream = resource.getInputStream();
            
            // Create audio input stream
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputStream);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
            
            // Close the input stream
            inputStream.close();
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