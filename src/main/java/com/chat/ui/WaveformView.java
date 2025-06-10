package com.chat.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.List;
import java.util.ArrayList;

public class WaveformView extends Canvas {
    private List<Double> amplitudes;
    private Color waveformColor;
    private double maxAmplitude;

    public WaveformView(double width, double height) {
        super(width, height);
        this.amplitudes = new ArrayList<>();
        this.waveformColor = Color.DODGERBLUE;
        this.maxAmplitude = 1.0;
    }

    public void setAmplitudes(List<Double> amplitudes) {
        this.amplitudes = amplitudes;
        if (!amplitudes.isEmpty()) {
            this.maxAmplitude = amplitudes.stream()
                .mapToDouble(Math::abs)
                .max()
                .orElse(1.0);
        }
        draw();
    }

    public void setWaveformColor(Color color) {
        this.waveformColor = color;
        draw();
    }

    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());
        gc.setStroke(waveformColor);
        gc.setLineWidth(2);

        if (amplitudes.isEmpty()) {
            return;
        }

        double xStep = getWidth() / (amplitudes.size() - 1);
        double yCenter = getHeight() / 2;
        double yScale = (getHeight() / 2) * 0.8; // 80% of half height

        gc.beginPath();
        for (int i = 0; i < amplitudes.size(); i++) {
            double x = i * xStep;
            double y = yCenter - (amplitudes.get(i) / maxAmplitude) * yScale;
            if (i == 0) {
                gc.moveTo(x, y);
            } else {
                gc.lineTo(x, y);
            }
        }
        gc.stroke();
    }

    public void clear() {
        amplitudes.clear();
        draw();
    }
} 