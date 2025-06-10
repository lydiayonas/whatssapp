package com.chat.service;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class VoiceRecordingService {
    private static final float SAMPLE_RATE = 44100;
    private static final int SAMPLE_SIZE_IN_BITS = 16;
    private static final int CHANNELS = 1;
    private static final boolean SIGNED = true;
    private static final boolean BIG_ENDIAN = true;
    private static final int COMPRESSION_QUALITY = 0; // 0-9, 0 being highest quality

    private final AtomicBoolean isRecording = new AtomicBoolean(false);
    private final AtomicLong startTime = new AtomicLong(0);
    private TargetDataLine line;
    private ByteArrayOutputStream out;
    private List<Double> amplitudes;
    private AudioFormat format;

    public VoiceRecordingService() {
        this.amplitudes = new ArrayList<>();
        this.format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, CHANNELS, SIGNED, BIG_ENDIAN);
    }

    public void startRecording() throws LineUnavailableException {
        if (isRecording.get()) {
            return;
        }

        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException("Line not supported");
        }

        line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();

        isRecording.set(true);
        startTime.set(System.currentTimeMillis());
        out = new ByteArrayOutputStream();
        amplitudes.clear();

        // Start recording in a separate thread
        new Thread(() -> {
            byte[] buffer = new byte[4096];
            while (isRecording.get()) {
                int count = line.read(buffer, 0, buffer.length);
                if (count > 0) {
                    out.write(buffer, 0, count);
                    // Calculate amplitude for visualization
                    calculateAmplitude(buffer, count);
                }
            }
        }).start();
    }

    private void calculateAmplitude(byte[] buffer, int count) {
        for (int i = 0; i < count; i += 2) {
            if (i + 1 < count) {
                short sample = (short) ((buffer[i + 1] << 8) | (buffer[i] & 0xFF));
                double amplitude = Math.abs(sample) / 32768.0; // Normalize to -1.0 to 1.0
                amplitudes.add(amplitude);
            }
        }
    }

    public byte[] stopRecording() {
        if (!isRecording.get()) {
            return null;
        }

        isRecording.set(false);
        line.stop();
        line.close();

        byte[] rawAudio = out.toByteArray();
        return compressAudio(rawAudio);
    }

    private byte[] compressAudio(byte[] rawAudio) {
        try {
            // Convert to MP3 or other compressed format
            // This is a placeholder - you'll need to add actual compression logic
            // For example, using LAME MP3 encoder or similar
            return rawAudio;
        } catch (Exception e) {
            e.printStackTrace();
            return rawAudio;
        }
    }

    public boolean isRecording() {
        return isRecording.get();
    }

    public long getRecordingDuration() {
        if (!isRecording.get()) {
            return 0;
        }
        return System.currentTimeMillis() - startTime.get();
    }

    public List<Double> getAmplitudes() {
        return new ArrayList<>(amplitudes);
    }

    public String getFormattedDuration() {
        long duration = getRecordingDuration();
        long seconds = (duration / 1000) % 60;
        long minutes = (duration / (1000 * 60)) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
} 