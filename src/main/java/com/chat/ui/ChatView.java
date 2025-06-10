package com.chat.ui;

import com.chat.client.ChatClient;
import com.chat.common.Message;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.time.format.DateTimeFormatter;

public class ChatView {
    private final BorderPane view;
    private final ListView<String> contactList;
    private final TextArea chatArea;
    private final TextField messageField;
    private final Button sendButton;
    private final Label statusLabel;
    private final ChatClient chatClient;
    private String currentContact;

    public ChatView(ChatClient chatClient) {
        this.chatClient = chatClient;
        
        view = new BorderPane();
        view.setPadding(new Insets(10));

        // Left side - Contact list
        VBox leftBox = new VBox(10);
        leftBox.setPadding(new Insets(10));
        leftBox.setPrefWidth(200);

        Text contactsTitle = new Text("Contacts");
        contactsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        contactList = new ListView<>();
        contactList.getItems().addAll("User1", "User2", "User3"); // Example contacts

        // Initialize statusLabel and chatArea before using them
        statusLabel = new Label("Select a contact to start chatting");
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        chatArea.setPrefRowCount(20);

        contactList.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    currentContact = newValue;
                    statusLabel.setText("Chatting with: " + currentContact);
                    chatArea.clear();
                }
            }
        );

        leftBox.getChildren().addAll(contactsTitle, contactList);

        // Center - Chat area
        VBox centerBox = new VBox(10);
        centerBox.setPadding(new Insets(10));

        HBox inputBox = new HBox(10);
        inputBox.setAlignment(Pos.CENTER_LEFT);
        messageField = new TextField();
        messageField.setPromptText("Type your message...");
        messageField.setPrefWidth(400);
        sendButton = new Button("Send");
        inputBox.getChildren().addAll(messageField, sendButton);

        centerBox.getChildren().addAll(statusLabel, chatArea, inputBox);

        // Right side - User info
        VBox rightBox = new VBox(10);
        rightBox.setPadding(new Insets(10));
        rightBox.setPrefWidth(150);

        Text userInfo = new Text("Logged in as:\n" + chatClient.getUsername());
        userInfo.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> handleLogout());

        rightBox.getChildren().addAll(userInfo, logoutButton);

        // Set up the main layout
        view.setLeft(leftBox);
        view.setCenter(centerBox);
        view.setRight(rightBox);

        setupEventHandlers();
    }

    private void setupEventHandlers() {
        sendButton.setOnAction(e -> sendMessage());
        messageField.setOnAction(e -> sendMessage());

        chatClient.setMessageHandler(this::handleIncomingMessage);
    }

    private void sendMessage() {
        if (currentContact == null) {
            showAlert("Please select a contact first");
            return;
        }

        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            chatClient.sendChatMessage(currentContact, message);
            appendMessage(chatClient.getUsername(), message);
            messageField.clear();
        }
    }

    private void handleIncomingMessage(Message message) {
        if (message.getType() == Message.MessageType.CHAT) {
            appendMessage(message.getSender(), message.getContent());
        }
    }

    private void appendMessage(String sender, String content) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String timestamp = java.time.LocalDateTime.now().format(formatter);
        chatArea.appendText(String.format("[%s] %s: %s%n", timestamp, sender, content));
    }

    private void handleLogout() {
        chatClient.logout();
        LoginView loginView = new LoginView();
        view.getScene().setRoot(loginView.getView());
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public BorderPane getView() {
        return view;
    }
} 