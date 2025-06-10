package com.chat.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class WelcomeView extends VBox {
    public WelcomeView() {
        setAlignment(Pos.CENTER);
        setSpacing(32);
        getStyleClass().add("welcome-view");

        // WhatsApp text logo
        Text logo = new Text("WhatsApp");
        logo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 48));
        logo.getStyleClass().add("welcome-logo");

        Label title = new Label("WhatsApp Desktop");
        title.getStyleClass().add("welcome-title");

        Label subtitle = new Label("Welcome to the official WhatsApp Desktop app.\nSimple. Secure. Reliable messaging.");
        subtitle.getStyleClass().add("welcome-subtitle");
        subtitle.setWrapText(true);
        subtitle.setAlignment(Pos.CENTER);

        Button startBtn = new Button("Start Messaging");
        startBtn.getStyleClass().add("welcome-start-btn");

        getChildren().addAll(logo, title, subtitle, startBtn);
    }
} 