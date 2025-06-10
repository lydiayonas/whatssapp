package com.chat.ui;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import java.io.File;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import com.chat.ui.MessageType;
import java.util.function.Consumer;

import com.chat.ui.User;
import javafx.scene.layout.StackPane;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.scene.layout.BorderPane;
import java.util.HashMap;
import java.util.Map;
import com.chat.common.Message;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import com.chat.service.VoiceRecordingService;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.io.IOException;
import javax.sound.sampled.LineUnavailableException;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import java.time.LocalDate;

public class ChatArea extends VBox {

    @FXML private TextField messageInput;
    @FXML private Button sendButton;
    @FXML private Button emojiButton;
    
    private MainView mainView;
    private ChatItemData currentChat;
    private User currentUser;
    private ObservableList<ChatItemData> chatListData;

    private VBox messageList;
    private ScrollPane messageScrollPane;
    private HBox inputArea;
    private Label typingIndicator;
    private HBox chatHeader;
    private ImageView chatHeaderAvatar;
    private VBox chatHeaderInfoBox;
    private Label chatHeaderName;
    private Label chatHeaderStatus;
    private HBox chatHeaderActions;
    private VBox emptyStateContainer; // Container for the empty state

    private VBox groupMemberListContainer; // Container for the group member list
    private ListView<User> groupMemberListView; // ListView to display members

    private List<MockMessage> currentConversation;
    private Consumer<MockMessage> onMessageSentCallback;
    private String currentChatName;

    private final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a");

    private BorderPane chatContentPane; // Use BorderPane for main chat area layout

    // Add isGroupChat field
    private boolean isGroupChat = false;

    // --- Add these fields for search bar ---
    private TextField searchBar;
    private HBox searchBarContainer;
    private List<MockMessage> filteredConversation = new ArrayList<>();

    private boolean isRecording = false;
    private LocalDateTime recordingStartTime;

    private final VoiceRecordingService voiceRecordingService;
    private Label recordingIndicator;
    private WaveformView waveformView;
    private Label durationLabel;
    private Timeline recordingUpdateTimeline;
    private HBox voiceControls; // Declare as a field
    private Button voiceButton; // Declare as a field

    // For undo functionality
    private MockMessage lastDeletedMessage;
    private int lastDeletedMessageIndex;
    private List<MockMessage> lastClearedMessages;
    private ChatItemData lastDeletedChat;
    private int lastDeletedChatIndex;

    private LocalDate lastMessageDate = null; // Field to keep track of the last message's date

    public ChatArea() {
        currentConversation = new ArrayList<>();
        getStyleClass().add("chat-area");

        // Initialize voice-related components early
        voiceRecordingService = new VoiceRecordingService();
        waveformView = new WaveformView(200, 40);
        durationLabel = new Label("00:00");
        setupVoiceRecording(); // This will now initialize 'voiceButton' and 'voiceControls'

        initializeComponents(); // This will now use the initialized 'voiceControls'
        setupEventHandlers();
    }

    private void initializeComponents() {
        // Initialize all components
        chatHeader = createChatHeader();
        messageScrollPane = new ScrollPane();
        messageScrollPane.setFitToWidth(true);
        messageScrollPane.setFitToHeight(true);
        messageScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        messageScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        messageScrollPane.getStyleClass().add("message-scroll-pane");

        // Set background image for the message scroll pane
        Image backgroundImage = new Image(getClass().getResourceAsStream("/com/chat/ui/background.png"));
        BackgroundSize backgroundSize = new BackgroundSize(100, 100, true, true, false, false);
        BackgroundImage chatBackground = new BackgroundImage(backgroundImage, BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.DEFAULT, backgroundSize);
        Background background = new Background(chatBackground);
        messageScrollPane.setBackground(background);

        messageList = new VBox(8);
        messageList.getStyleClass().add("message-list");
        messageScrollPane.setContent(messageList);

        inputArea = createInputArea();

        // Empty State Container
        emptyStateContainer = new VBox();
        emptyStateContainer.setAlignment(Pos.CENTER);
        emptyStateContainer.setPadding(new Insets(0)); // No padding needed here, padding is on the emptyState VBox itself
        emptyStateContainer.getChildren().add(createEmptyState());

        // Initialize group member list container (initially hidden)
        groupMemberListContainer = new VBox();
        groupMemberListContainer.getStyleClass().add("group-member-list-container");
        groupMemberListContainer.setAlignment(Pos.TOP_RIGHT);
        groupMemberListContainer.setManaged(false); // Hide and don't reserve space
        groupMemberListContainer.setVisible(false);

        groupMemberListView = new ListView<>();
        groupMemberListView.getStyleClass().add("group-member-list-view");
        // Set a custom cell factory to display User names
        groupMemberListView.setCellFactory(param -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(user.name); // Display the user's name
                    // You can add user avatars here later if needed
                    // ImageView avatarView = new ImageView(new Image(getClass().getResourceAsStream(user.avatarPath)));
                    // avatarView.setFitWidth(20); // Adjust size as needed
                    // avatarView.setFitHeight(20); // Adjust size as needed
                    // setGraphic(avatarView);
                }
            }
        });

        groupMemberListContainer.getChildren().add(groupMemberListView);

        // --- Add search bar above message list, initially hidden ---
        searchBar = new TextField();
        searchBar.setPromptText("Search messages...");
        searchBar.setVisible(false);
        searchBar.setManaged(false);
        searchBar.textProperty().addListener((obs, oldVal, newVal) -> searchMessages(newVal));
        Button closeSearchButton = new Button("✖");
        closeSearchButton.setOnAction(e -> {
            searchBar.setVisible(false);
            searchBar.setManaged(false);
            searchBarContainer.setVisible(false);
            searchBarContainer.setManaged(false);
            searchBar.clear();
            messageList.getChildren().clear();
            for (MockMessage message : currentConversation) {
                addMessageBubble(message);
            }
        });
        searchBarContainer = new HBox(searchBar, closeSearchButton);
        searchBarContainer.setPadding(new Insets(4, 8, 4, 8));
        searchBarContainer.setVisible(false);
        searchBarContainer.setManaged(false);
        VBox messageArea = new VBox(searchBarContainer, messageScrollPane);
        VBox.setVgrow(messageScrollPane, Priority.ALWAYS);

        // Add chat header, message area, input area, and empty state container to the main VBox
        // Add group member list container as an overlay using StackPane
        chatContentPane = new BorderPane();
        chatContentPane.setTop(chatHeader);
        chatContentPane.setCenter(messageArea);
        chatContentPane.setBottom(inputArea);
        chatContentPane.getStyleClass().add("chat-content-pane"); // Optional: for styling
        // Removed initial setVisible(false) and setManaged(false) - now controlled by showChatContent

        // Use StackPane to layer the chat content, empty state, and member list
        StackPane mainStack = new StackPane();
        mainStack.getChildren().addAll(chatContentPane, emptyStateContainer, groupMemberListContainer);
        mainStack.getStyleClass().add("chat-main-stack"); // Optional: for styling

        // Set the StackPane as the single child of the ChatArea VBox
        this.getChildren().clear();
        this.getChildren().add(mainStack);
        VBox.setVgrow(mainStack, Priority.ALWAYS);
    }

    private void layoutComponents() {
        // Components will be added and removed dynamically based on the selected chat.
        // This method is now empty as layout is managed in initializeComponents with StackPane.
    }

    private void setupEventHandlers() {
        sendButton.setOnAction(e -> sendMessage());
        messageInput.setOnAction(e -> sendMessage());
        // attachmentButton.setOnAction(e -> handleAttachment()); // Removed as attachment is now handled by icon
        // Removed emojiButton.setOnAction(e -> handleEmojiMessage()); as emoji button is no longer a separate element
    }

    private HBox createChatHeader() {
        HBox chatHeader = new HBox(10);
        chatHeader.getStyleClass().add("chat-header");
        chatHeader.setAlignment(Pos.CENTER_LEFT);

        // Current chat avatar
        ImageView chatAvatar = new ImageView(new Image(getClass().getResourceAsStream("/com/chat/ui/avatar_placeholder.png")));
        chatAvatar.setFitWidth(40);
        chatAvatar.setFitHeight(40);
        chatAvatar.getStyleClass().add("chat-header-avatar");

        // Chat name and status
        VBox chatInfo = new VBox(2);
        chatInfo.getStyleClass().add("chat-header-info-box");
        chatHeaderName = new Label("Select a Chat");
        chatHeaderName.getStyleClass().add("chat-header-name");
        chatHeaderStatus = new Label("Online");
        chatHeaderStatus.getStyleClass().add("chat-header-status");
        chatInfo.getChildren().addAll(chatHeaderName, chatHeaderStatus);

        chatHeader.getChildren().addAll(chatAvatar, chatInfo);
        HBox.setHgrow(chatInfo, Priority.ALWAYS);

        // Search button
        Button searchButton = new Button();
        ImageView searchIcon = new ImageView(new Image(getClass().getResourceAsStream("/com/chat/ui/search_icon.svg")));
        searchIcon.setFitWidth(20);
        searchIcon.setFitHeight(20);
        searchButton.setGraphic(searchIcon);
        searchButton.getStyleClass().add("icon-button");
        searchButton.setOnAction(e -> {
            // Implement search functionality
            System.out.println("Search button clicked!");
        });

        chatHeader.getChildren().add(searchButton);

        return chatHeader;
    }

    private HBox createInputArea() {
        HBox inputArea = new HBox(8);
        inputArea.getStyleClass().add("input-area");
        inputArea.setAlignment(Pos.CENTER);
        inputArea.setPadding(new Insets(8));

        // Message input field
        messageInput = new TextField();
        messageInput.setPromptText("Type a message");
        messageInput.getStyleClass().add("message-input");
        HBox.setHgrow(messageInput, Priority.ALWAYS);

        // Attachment icon
        ImageView attachmentIcon = new ImageView(new Image(getClass().getResourceAsStream("/com/chat/ui/attachment_icon.png")));
        attachmentIcon.setFitWidth(20);
        attachmentIcon.setFitHeight(20);
        attachmentIcon.setOnMouseClicked(e -> showFileChooser());
        attachmentIcon.getStyleClass().add("attachment-icon-button"); // For styling if needed

        // Send and Voice button - dynamically switch
        sendButton = new Button("➤"); // Unicode for send icon
        sendButton.getStyleClass().add("send-button");
        sendButton.setManaged(false); // Initially hidden
        sendButton.setVisible(false);
        sendButton.setOnAction(e -> sendMessage()); // Add action for send button

        // Listener to switch between voice and send button
        messageInput.textProperty().addListener((obs, oldText, newText) -> {
            boolean hasText = newText != null && !newText.trim().isEmpty();
            sendButton.setManaged(hasText);
            sendButton.setVisible(hasText);
            voiceControls.setManaged(!hasText);
            voiceControls.setVisible(!hasText);
        });

        // Ensure voice button is visible initially if no text
        voiceControls.setManaged(true);
        voiceControls.setVisible(true);

        inputArea.getChildren().addAll(attachmentIcon, messageInput, voiceControls, sendButton);
        return inputArea;
    }

    public void loadConversation(List<MockMessage> conversation) {
        this.currentConversation = conversation;
        messageList.getChildren().clear();
        lastMessageDate = null; // Reset date for new conversation load
        if (conversation.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
            for (MockMessage message : conversation) {
                addMessageBubble(message);
            }
            // Scroll to the bottom after loading messages
            Platform.runLater(() -> messageScrollPane.setVvalue(1.0));
        }
    }

    public void addMessage(MockMessage message) {
        currentConversation.add(message);
        addMessageBubble(message);

        // Scroll to the bottom with animation
        messageScrollPane.applyCss();
        messageScrollPane.layout();
        // Using Platform.runLater to ensure layout updates before scrolling
        Platform.runLater(() -> {
            messageScrollPane.setVvalue(1.0);
            messageScrollPane.vvalueProperty().unbind();
        });
    }

    public void sendMessage() {
        String content = messageInput.getText().trim();
        if (!content.isEmpty()) {
            // Create a mock message for UI display
            MockMessage mockMessage = new MockMessage(
                currentUser,
                content,
                LocalDateTime.now(),
                MessageType.TEXT
            );
            
            // Add the message to the UI
            addMessage(mockMessage);
            
            // Clear the input field
            messageInput.clear();
            
            // Move chat to top of list if it exists
            if (chatListData != null) {
                chatListData.remove(currentChat);
                chatListData.add(0, currentChat);
            }
        }
    }

    private void showAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    private void simulateResponse() {
        // Simulate a response after 1 second
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                javafx.application.Platform.runLater(() -> {
                    if (currentChat != null) {
                        MockMessage response = new MockMessage(
                            new User(2, "Bot", "/com/chat/ui/bot_avatar.png", true), // Corrected path
                            "This is an automated response",
                            LocalDateTime.now(),
                            MessageType.TEXT
                        );
                        currentChat.getMessages().add(response);
                        addMessageBubble(response);
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Method to update the chat header
    public void setChatHeader(String name, String status, String avatarPath) {
        chatHeaderName.setText(name);
        chatHeaderStatus.setText(status);
        if (avatarPath != null) {
            try {
                Image avatarImage = new Image(getClass().getResourceAsStream(avatarPath));
                chatHeaderAvatar.setImage(avatarImage);
            } catch (Exception e) {
                System.err.println("Error loading avatar: " + e.getMessage());
            }
        }
    }

    private void addMessageBubble(MockMessage message) {
        // Check for date change and add separator
        LocalDate messageDate = message.timestamp.toLocalDate();
        if (lastMessageDate == null || !messageDate.isEqual(lastMessageDate)) {
            addDateSeparator(messageDate);
            lastMessageDate = messageDate;
        }

        MessageBubble bubble;
        if (message.type == MessageType.VOICE) {
            bubble = createVoiceMessageBubble(message);
        } else {
            // Pass fileContent and fileType to the MessageBubble constructor
            bubble = new MessageBubble(
                message.sender.name,
                message.content,
                message.timestamp,
                message.isSent,
                message.isRead,
                message.type,
                message.replyPreview,
                message.forwardedFrom,
                message.reactions,
                isGroupChat,
                message.fileContent, // Pass fileContent
                message.fileType     // Pass fileType
            );
        }

        // Add context menu for deleting message with confirmation
        ContextMenu messageContextMenu = new ContextMenu();
        MenuItem deleteMessageMenuItem = new MenuItem("Delete Message");
        deleteMessageMenuItem.setOnAction(event -> {
            Alert confirmDeleteAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDeleteAlert.setTitle("Confirm Deletion");
            confirmDeleteAlert.setHeaderText(null);
            confirmDeleteAlert.setContentText("Are you sure you want to delete this message?");

            confirmDeleteAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    Platform.runLater(() -> {
                        int index = messageList.getChildren().indexOf(bubble);
                        if (index != -1) {
                            lastDeletedMessage = currentConversation.remove(index);
                            lastDeletedMessageIndex = index;
                            messageList.getChildren().remove(bubble);
                            showUndoAlert("Message deleted.", () -> {
                                if (lastDeletedMessage != null) {
                                    currentConversation.add(lastDeletedMessageIndex, lastDeletedMessage);
                                    messageList.getChildren().add(lastDeletedMessageIndex, new MessageBubble(
                                        lastDeletedMessage.sender.name,
                                        lastDeletedMessage.content,
                                        lastDeletedMessage.timestamp,
                                        lastDeletedMessage.isSent,
                                        lastDeletedMessage.isRead,
                                        lastDeletedMessage.type,
                                        lastDeletedMessage.replyPreview,
                                        lastDeletedMessage.forwardedFrom,
                                        lastDeletedMessage.reactions,
                                        isGroupChat,
                                        lastDeletedMessage.fileContent,
                                        lastDeletedMessage.fileType
                                    ));
                                    lastDeletedMessage = null; // Clear the undo state
                                }
                            });
                        }
                    });
                }
            });
        });
        messageContextMenu.getItems().add(deleteMessageMenuItem);

        bubble.setOnContextMenuRequested(event -> messageContextMenu.show(bubble, event.getScreenX(), event.getScreenY()));

        Platform.runLater(() -> {
            messageList.getChildren().add(bubble);
            messageScrollPane.setVvalue(1.0); // Scroll to bottom on new message
        });
    }

    private void addDateSeparator(LocalDate date) {
        Label dateLabel = new Label(date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        dateLabel.getStyleClass().add("date-separator");
        HBox separatorContainer = new HBox(dateLabel);
        separatorContainer.setAlignment(Pos.CENTER);
        separatorContainer.setPadding(new Insets(10, 0, 10, 0));
        messageList.getChildren().add(separatorContainer);
    }

    private void showChatContent() {
        chatContentPane.setVisible(true);
        chatContentPane.setManaged(true);
        emptyStateContainer.setVisible(false);
        emptyStateContainer.setManaged(false);
    }

    private VBox createEmptyState() {
        VBox emptyState = new VBox(20);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.getStyleClass().add("empty-state");
        emptyState.setPadding(new Insets(20));

        Label message = new Label("Choose chat to start messaging");
        message.getStyleClass().add("empty-state-text");

        emptyState.getChildren().add(message);
        return emptyState;
    }

    // MockMessage class
    public static class MockMessage {
        public final User sender;
        public final String content;
        public final LocalDateTime timestamp;
        public final MessageType type;
        public final String replyPreview;
        public final String forwardedFrom;
        public final List<String> reactions;
        public final boolean isSent;
        public final boolean isRead;
        public final byte[] fileContent;
        public final String fileType;

        // Updated constructor to include fileContent and fileType
        public MockMessage(User sender, String content, LocalDateTime timestamp, MessageType type, String replyPreview, String forwardedFrom, List<String> reactions, boolean isSent, boolean isRead, byte[] fileContent, String fileType) {
            this.sender = sender;
            this.content = content;
            this.timestamp = timestamp;
            this.type = type;
            this.replyPreview = replyPreview;
            this.forwardedFrom = forwardedFrom;
            this.reactions = reactions;
            this.isSent = isSent;
            this.isRead = isRead;
            this.fileContent = fileContent;
            this.fileType = fileType;
        }

        // Existing constructor (delegates to the full constructor)
        public MockMessage(User sender, String content, LocalDateTime timestamp, MessageType type) {
            this(sender, content, timestamp, type, null, null, null, false, false, null, null);
        }

        public int getSenderId() {
            return sender.id;
        }
    }

    // Method to set the message sent callback
    public void setOnMessageSent(Consumer<MockMessage> callback) {
        this.onMessageSentCallback = callback;
    }

    // Method to display the group member list
    public void displayGroupMembers(List<User> members) {
        groupMemberListView.getItems().setAll(members);
        groupMemberListContainer.setManaged(true);
        groupMemberListContainer.setVisible(true);
        // Bring the member list container to the front
        groupMemberListContainer.toFront();
        // Ensure chat content is behind the member list
        chatContentPane.toBack();
    }

    // Method to hide the group member list
    public void hideGroupMemberList() {
        groupMemberListContainer.setManaged(false);
        groupMemberListContainer.setVisible(false);
        // Bring the chat content back to the front if the member list is hidden
        chatContentPane.toFront();
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        // Ensure the empty state container and member list container are sized correctly within the stack pane
        // This might be needed if they don't automatically fill the available space
        emptyStateContainer.setPrefSize(this.getWidth(), this.getHeight());
        groupMemberListContainer.setPrefSize(this.getWidth(), this.getHeight());
    }

    public void setGroupChat(boolean isGroupChat) {
        this.isGroupChat = isGroupChat;
    }

    public void setCurrentChat(ChatItemData chat) {
        this.currentChat = chat;
        if (chat != null) {
            showChatContent();
            updateMessageDisplay();
        } else {
            showEmptyState();
        }
    }

    private void showEmptyState() {
        emptyStateContainer.setVisible(true);
        emptyStateContainer.setManaged(true);
        messageScrollPane.setVisible(false);
        messageScrollPane.setManaged(false);
    }

    private void hideEmptyState() {
        emptyStateContainer.setVisible(false);
        emptyStateContainer.setManaged(false);
        messageScrollPane.setVisible(true);
        messageScrollPane.setManaged(true);
    }

    public void loadConversationFromDb(List<Message> messages) {
        List<MockMessage> mockMessages = new ArrayList<>();
        for (Message msg : messages) {
            MockMessage mockMsg = new MockMessage(
                new User(1, msg.getSender(), "/com/chat/ui/icons/default_avatar.png", true),
                msg.getContent(),
                msg.getTimestamp(),
                MessageType.TEXT
            );
            mockMessages.add(mockMsg);
        }
        loadConversation(mockMessages);
    }

    private void showSearchBar() {
        searchBar.setVisible(true);
        searchBar.setManaged(true);
        searchBarContainer.setVisible(true);
        searchBarContainer.setManaged(true);
        searchBar.requestFocus();
    }

    private void searchMessages(String query) {
        messageList.getChildren().clear();
        if (query == null || query.isEmpty()) {
            for (MockMessage message : currentConversation) {
                addMessageBubble(message);
            }
        } else {
            for (MockMessage message : currentConversation) {
                if (message.content != null && message.content.toLowerCase().contains(query.toLowerCase())) {
                    addMessageBubble(message);
                }
            }
        }
    }

    private void showFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File");
        File selectedFile = fileChooser.showOpenDialog(getScene().getWindow());

        if (selectedFile != null) {
            try {
                byte[] fileContent = Files.readAllBytes(selectedFile.toPath());
                String fileName = selectedFile.getName();
                String fileExtension = "";
                int dotIndex = fileName.lastIndexOf('.');
                if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
                    fileExtension = fileName.substring(dotIndex + 1).toLowerCase();
                }

                MessageType messageType;
                // Determine if it's an image based on common image extensions
                if (fileExtension.equals("jpg") || fileExtension.equals("jpeg") || fileExtension.equals("png") || fileExtension.equals("gif")) {
                    messageType = MessageType.IMAGE;
                } else {
                    messageType = MessageType.FILE;
                }
                
                // Create MockMessage with file content and type
                MockMessage attachmentMessage = new MockMessage(
                    currentUser,
                    fileName,
                    LocalDateTime.now(),
                    messageType,
                    null, null, null, true, false, // replyPreview, forwardedFrom, reactions, isSent, isRead
                    fileContent,
                    fileExtension
                );
                addMessage(attachmentMessage);
            } catch (IOException e) {
                showAlert("Error", "Could not read file: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void startVoiceRecording() { // Removed voiceButton parameter
        if (!isRecording) {
            isRecording = true;
            recordingStartTime = LocalDateTime.now();
            
            // Update voice button graphic to stop icon
            voiceButton.setText("⏹"); // Using Unicode for now
            voiceButton.getStyleClass().add("recording");
            
            // TODO: Implement actual voice recording
            // For now, we'll just simulate it with a timer
            new Thread(() -> {
                try {
                    Thread.sleep(5000); // Simulate 5 seconds of recording
                    Platform.runLater(() -> {
                        stopVoiceRecording();
                        // Create and send a voice message
                        MockMessage voiceMessage = new MockMessage(
                            currentUser,
                            "Voice Message",
                            LocalDateTime.now(),
                            MessageType.VOICE,
                            null,
                            null,
                            null,
                            true,
                            false,
                            null,
                            null
                        );
                        if (onMessageSentCallback != null) {
                            onMessageSentCallback.accept(voiceMessage);
                        }
                        addMessageBubble(voiceMessage);
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            stopVoiceRecording();
        }
    }

    private void stopVoiceRecording() { // Removed voiceButton parameter
        isRecording = false;
        voiceButton.setText("🎤"); // Using Unicode for now
        voiceButton.getStyleClass().remove("recording");
    }

    public void setChatListData(ObservableList<ChatItemData> chatListData) {
        this.chatListData = chatListData;
    }

    private void updateMessageDisplay() {
        if (currentChat != null) {
            messageList.getChildren().clear(); // Use messageList here
            if (currentChat.getMessages() != null) {
                for (MockMessage message : currentChat.getMessages()) {
                    addMessageBubble(message);
                }
            }
            showChatContent();
        } else {
            // If no chat is selected, show empty state
            emptyStateContainer.setVisible(true);
            emptyStateContainer.setManaged(true);
            chatContentPane.setVisible(false);
            chatContentPane.setManaged(false);
        }
    }

    private void handleEmojiMessage() {
        // TODO: Implement emoji message functionality
        System.out.println("Emoji message button clicked");
    }

    private void addMessageToUI(MockMessage message) {
        addMessageBubble(message);
        // Scroll to the bottom when a new message is added
        Platform.runLater(() -> messageScrollPane.setVvalue(1.0));
    }

    public void setMainView(MainView mainView) {
        this.mainView = mainView;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    private void convertMessages(List<Message> messages) {
        List<MockMessage> mockMessages = new ArrayList<>();
        for (Message msg : messages) {
            MockMessage mockMsg = new MockMessage(
                new User(1, msg.getSender(), "/com/chat/ui/icons/default_avatar.png", true),
                msg.getContent(),
                msg.getTimestamp(),
                MessageType.TEXT
            );
            mockMessages.add(mockMsg);
        }
        currentConversation = mockMessages;
    }

    private void setupVoiceRecording() {
        voiceButton = new Button("🎤");
        voiceButton.getStyleClass().addAll("voice-button", "large-circular-button"); // Added 'large-circular-button'
        
        // Set initial visibility of waveform and duration label
        waveformView.setVisible(false);
        durationLabel.setVisible(false);

        // Create and assign to the class field voiceControls
        voiceControls = new HBox(10);
        voiceControls.getStyleClass().add("voice-controls");
        voiceControls.getChildren().addAll(voiceButton, waveformView, durationLabel);

        voiceButton.setOnMousePressed(event -> {
            try {
                voiceRecordingService.startRecording();
                waveformView.setVisible(true);
                durationLabel.setVisible(true);
                voiceButton.getStyleClass().add("recording");
                
                // Start updating duration and waveform
                startRecordingUpdates();
            } catch (LineUnavailableException e) {
                showAlert("Error", "Could not access microphone: " + e.getMessage());
            }
        });

        voiceButton.setOnMouseReleased(event -> {
            if (voiceRecordingService.isRecording()) {
                stopRecordingUpdates();
                byte[] audioData = voiceRecordingService.stopRecording();
                waveformView.setVisible(false);
                durationLabel.setVisible(false);
                voiceButton.getStyleClass().remove("recording");

                if (audioData != null && audioData.length > 0) {
                    try {
                        // Create and send the voice message
                        MockMessage voiceMessage = new MockMessage(
                            currentUser,
                            "Voice Message",
                            LocalDateTime.now(),
                            MessageType.VOICE,
                            null,
                            null,
                            null,
                            true,
                            false,
                            audioData,
                            "audio/wav"
                        );

                        // Add to UI
                        addMessage(voiceMessage);

                        // Send via WebSocket
                        if (onMessageSentCallback != null) {
                            onMessageSentCallback.accept(voiceMessage);
                        }
                    } catch (Exception e) {
                        showAlert("Error", "Could not save voice message: " + e.getMessage());
                    }
                }
            }
        });

        // Removed inputArea.getChildren().add(voiceControls); as it's now added via rightButtonsStack in createInputArea
    }

    private void startRecordingUpdates() {
        recordingUpdateTimeline = new Timeline(
            new KeyFrame(Duration.millis(100), event -> {
                // Update duration
                durationLabel.setText(voiceRecordingService.getFormattedDuration());
                
                // Update waveform
                List<Double> amplitudes = voiceRecordingService.getAmplitudes();
                waveformView.setAmplitudes(amplitudes);
            })
        );
        recordingUpdateTimeline.setCycleCount(Timeline.INDEFINITE);
        recordingUpdateTimeline.play();
    }

    private void stopRecordingUpdates() {
        if (recordingUpdateTimeline != null) {
            recordingUpdateTimeline.stop();
        }
    }

    private MessageBubble createVoiceMessageBubble(MockMessage message) {
        MessageBubble bubble = new MessageBubble(
            message.sender.name,
            "", // Changed content to empty string, as waveform visualizes it
            message.timestamp,
            message.isSent,
            message.isRead,
            com.chat.ui.MessageType.VOICE,
            null,
            null,
            null,
            false,
            message.fileContent,
            message.fileType
        );

        // Create waveform for the recorded message
        WaveformView messageWaveform = new WaveformView(150, 30);
        messageWaveform.setWaveformColor(message.isSent ? Color.DODGERBLUE : Color.GRAY);
        
        // Calculate amplitudes from the audio data
        List<Double> amplitudes = calculateAmplitudesFromAudio(message.fileContent);
        messageWaveform.setAmplitudes(amplitudes);

        // Add play button and waveform
        HBox controls = new HBox(10);
        controls.getStyleClass().add("voice-message-controls");
        
        Button playButton = new Button("▶");
        playButton.getStyleClass().add("play-button");
        playButton.setOnAction(event -> {
            try {
                // Create temporary file for playback
                Path tempFile = Files.createTempFile("voice_", ".wav");
                try (FileOutputStream fos = new FileOutputStream(tempFile.toFile())) {
                    fos.write(message.fileContent);
                }

                // Create and play media
                Media media = new Media(tempFile.toUri().toString());
                MediaPlayer mediaPlayer = new MediaPlayer(media);
                
                // Update play button during playback
                playButton.setText("⏸");
                mediaPlayer.setOnEndOfMedia(() -> {
                    playButton.setText("▶");
                    mediaPlayer.dispose();
                    try {
                        Files.delete(tempFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                
                mediaPlayer.play();
            } catch (IOException e) {
                showAlert("Error", "Could not play voice message: " + e.getMessage());
            }
        });

        controls.getChildren().addAll(playButton, messageWaveform);
        bubble.getChildren().add(controls);
        
        return bubble;
    }

    private List<Double> calculateAmplitudesFromAudio(byte[] audioData) {
        List<Double> amplitudes = new ArrayList<>();
        for (int i = 0; i < audioData.length; i += 2) {
            if (i + 1 < audioData.length) {
                short sample = (short) ((audioData[i + 1] << 8) | (audioData[i] & 0xFF));
                double amplitude = Math.abs(sample) / 32768.0;
                amplitudes.add(amplitude);
            }
        }
        return amplitudes;
    }

    private com.chat.ui.MessageType convertMessageType(com.chat.model.Message.MessageType type) {
        return switch (type) {
            case TEXT -> com.chat.ui.MessageType.TEXT;
            case IMAGE -> com.chat.ui.MessageType.IMAGE;
            case VOICE -> com.chat.ui.MessageType.VOICE;
            default -> com.chat.ui.MessageType.TEXT;
        };
    }

    private void clearCurrentChatHistory() {
        if (currentChat != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to clear the chat history?", ButtonType.YES, ButtonType.NO);
            confirm.setHeaderText(null);
            confirm.showAndWait().ifPresent(result -> {
                if (result == ButtonType.YES) {
                    lastClearedMessages = new ArrayList<>(currentChat.getMessages());
                    currentChat.getMessages().clear();
                    if (currentConversation != null) {
                        currentConversation.clear();
                    }
                    updateMessageDisplay();
                    showUndoAlert("Chat history cleared", () -> {
                        // Undo action
                        currentChat.getMessages().addAll(lastClearedMessages);
                        if (currentConversation != null) {
                            currentConversation.addAll(lastClearedMessages);
                        }
                        updateMessageDisplay();
                    });
                }
            });
        }
    }

    private void deleteCurrentChat() {
        if (chatListData != null && currentChat != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this chat? This cannot be undone.", ButtonType.YES, ButtonType.NO);
            confirm.setHeaderText(null);
            confirm.showAndWait().ifPresent(result -> {
                if (result == ButtonType.YES) {
                    lastDeletedChat = currentChat;
                    lastDeletedChatIndex = chatListData.indexOf(currentChat);
                    chatListData.remove(currentChat);
                    setCurrentChat(null);
                    updateMessageDisplay();
                    showUndoAlert("Chat deleted", () -> {
                        // Undo action
                        chatListData.add(lastDeletedChatIndex, lastDeletedChat);
                        setCurrentChat(lastDeletedChat);
                        updateMessageDisplay();
                    });
                }
            });
        }
    }

    // Show a temporary alert with Undo button
    private void showUndoAlert(String message, Runnable undoAction) {
        Alert undoAlert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK, new ButtonType("Undo"));
        undoAlert.setHeaderText(null);
        undoAlert.setTitle("Undo");
        undoAlert.show();
        // Handle Undo
        undoAlert.resultProperty().addListener((obs, oldResult, newResult) -> {
            if (newResult != null && "Undo".equals(newResult.getText())) {
                undoAction.run();
            }
        });
        // Auto-close after 4 seconds if not clicked
        new Thread(() -> {
            try {
                Thread.sleep(4000);
                Platform.runLater(() -> {
                    if (undoAlert.isShowing()) {
                        undoAlert.close();
                    }
                });
            } catch (InterruptedException ignored) {}
        }).start();
    }
}