package com.chat.ui;

import com.chat.client.ChatClient;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class LoginView {
    private final VBox view;
    private final TextField usernameField;
    private final PasswordField passwordField;
    private final Button loginButton;
    private final Button registerButton;
    private final Text messageText;
    private ChatClient chatClient;

    public LoginView() {
        view = new VBox(10);
        view.setPadding(new Insets(20));
        view.setAlignment(Pos.CENTER);

        Text title = new Text("Welcome to Chat");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);

        Label usernameLabel = new Label("Username:");
        usernameField = new TextField();
        grid.add(usernameLabel, 0, 0);
        grid.add(usernameField, 1, 0);

        Label passwordLabel = new Label("Password:");
        passwordField = new PasswordField();
        grid.add(passwordLabel, 0, 1);
        grid.add(passwordField, 1, 1);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        loginButton = new Button("Login");
        registerButton = new Button("Register");
        buttonBox.getChildren().addAll(loginButton, registerButton);

        messageText = new Text();
        messageText.setFill(javafx.scene.paint.Color.RED);

        view.getChildren().addAll(title, grid, buttonBox, messageText);

        setupEventHandlers();
    }

    private void setupEventHandlers() {
        loginButton.setOnAction(e -> handleLogin());
        registerButton.setOnAction(e -> handleRegister());
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageText.setText("Please enter both username and password");
            return;
        }

        try {
            chatClient = new ChatClient();
            chatClient.setMessageHandler(this::handleServerMessage);
            chatClient.login(username, password);
        } catch (Exception ex) {
            messageText.setText("Connection error: " + ex.getMessage());
        }
    }

    private void handleRegister() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageText.setText("Please enter both username and password");
            return;
        }

        try {
            chatClient = new ChatClient();
            chatClient.setMessageHandler(this::handleServerMessage);
            chatClient.register(username, password);
        } catch (Exception ex) {
            messageText.setText("Connection error: " + ex.getMessage());
        }
    }

    private void handleServerMessage(com.chat.common.Message message) {
        if (message.getType() == com.chat.common.Message.MessageType.ERROR) {
            messageText.setText(message.getContent());
        } else if (message.getContent().equals("Login successful") || 
                  message.getContent().equals("Registration successful")) {
            // Switch to chat view
            ChatView chatView = new ChatView(chatClient);
            view.getScene().setRoot(chatView.getView());
        }
    }

    public VBox getView() {
        return view;
    }
} 