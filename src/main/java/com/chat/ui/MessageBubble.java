package com.chat.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import com.chat.ui.MessageType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import java.util.function.Consumer;
import javafx.stage.FileChooser;
import java.io.FileOutputStream;
import java.io.IOException;
import javafx.scene.control.Button;
import java.io.File;
import java.io.ByteArrayInputStream;

public class MessageBubble extends HBox {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private String sender;
    private String content;
    private LocalDateTime timestamp;
    private boolean isSent;
    private boolean isRead;
    private MessageType type;
    private String replyPreview;
    private String forwardedFrom;
    private List<String> reactions;
    private Label timestampLabel;
    private Label readReceiptLabel;
    private VBox replyPreviewBox;
    private VBox forwardedFromBox;
    private VBox fileBox;
    private VBox voiceBox;
    private ImageView imageView;
    private HBox reactionsRow;
    private VBox bubbleContent;
    private Text messageText;
    private boolean isGroupChat;
    private byte[] fileContent;
    private String fileType;

    public MessageBubble(String sender, String content, LocalDateTime timestamp, boolean isSent, boolean isRead, MessageType type, String replyPreview, String forwardedFrom, List<String> reactions, boolean isGroupChat) {
        this(sender, content, timestamp, isSent, isRead, type, replyPreview, forwardedFrom, reactions, isGroupChat, null, null);
    }

    public MessageBubble(String sender, String content, LocalDateTime timestamp, boolean isSent, boolean isRead, MessageType type, String replyPreview, String forwardedFrom, List<String> reactions, boolean isGroupChat, byte[] fileContent, String fileType) {
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
        this.isSent = isSent;
        this.isRead = isRead;
        this.type = type;
        this.replyPreview = replyPreview;
        this.forwardedFrom = forwardedFrom;
        this.reactions = reactions;
        this.isGroupChat = isGroupChat;
        this.fileContent = fileContent;
        this.fileType = fileType;
        getStyleClass().add("message-bubble");
        getStyleClass().add(isSent ? "outgoing" : "incoming");

        HBox messageBox = new HBox(8);
        messageBox.getStyleClass().add("message-box");
        messageBox.setAlignment(isSent ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        // Avatar for incoming messages or group chat messages
        if (!isSent || isGroupChat) {
            Image avatarImg;
            try {
                java.io.InputStream avatarStream = getClass().getResourceAsStream("/com/chat/ui/avatar_placeholder.png");
                avatarImg = avatarStream == null
                        ? new Image(getClass().getResourceAsStream("/com/chat/ui/avatar_placeholder.png"))
                        : new Image(avatarStream);
            } catch (Exception e) {
                avatarImg = new Image(getClass().getResourceAsStream("/com/chat/ui/avatar_placeholder.png"));
            }
            ImageView avatar = new ImageView(avatarImg);
            avatar.setFitWidth(36);
            avatar.setFitHeight(36);
            avatar.getStyleClass().add("bubble-avatar");
            messageBox.getChildren().add(avatar);
        }

        bubbleContent = new VBox(4);
        bubbleContent.getStyleClass().add("bubble-content");
        bubbleContent.setMaxWidth(400);

        // Show sender name for group chat messages
        if (isGroupChat && !isSent) {
            Label senderLabel = new Label(sender);
            senderLabel.getStyleClass().add("message-sender");
            bubbleContent.getChildren().add(senderLabel);
        }

        // Forwarded from
        if (forwardedFrom != null && !forwardedFrom.isEmpty()) {
            forwardedFromBox = new VBox();
            forwardedFromBox.getStyleClass().add("bubble-forwarded-from");
            Label forwardedLabel = new Label("Forwarded from " + forwardedFrom);
            forwardedLabel.getStyleClass().add("bubble-forwarded-label");
            forwardedLabel.setWrapText(true);
            forwardedLabel.setMaxWidth(300);
            forwardedFromBox.getChildren().add(forwardedLabel);
            bubbleContent.getChildren().add(forwardedFromBox);
        }

        // Reply preview
        if (replyPreview != null && !replyPreview.isEmpty()) {
            replyPreviewBox = new VBox();
            replyPreviewBox.getStyleClass().add("bubble-reply-preview");
            Label replyLabel = new Label(replyPreview);
            replyLabel.getStyleClass().add("bubble-reply-label");
            replyLabel.setWrapText(true);
            replyLabel.setMaxWidth(300);
            replyPreviewBox.getChildren().add(replyLabel);
            bubbleContent.getChildren().add(replyPreviewBox);
        }

        // Message content based on type
        switch (type) {
            case TEXT:
                TextFlow textFlow = new TextFlow();
                textFlow.getStyleClass().add("bubble-text-flow");
                textFlow.setPrefWidth(Region.USE_COMPUTED_SIZE);
                textFlow.setMinWidth(Region.USE_PREF_SIZE);
                textFlow.setMaxWidth(350);

                messageText = new Text(content);
                messageText.getStyleClass().add("bubble-text");
                messageText.setWrappingWidth(0);

                textFlow.getChildren().add(messageText);
                bubbleContent.getChildren().add(textFlow);
                break;
            case IMAGE:
                if (fileContent != null && fileContent.length > 0) {
                    try (ByteArrayInputStream bis = new ByteArrayInputStream(fileContent)) {
                        Image image = new Image(bis);
                        imageView = new ImageView(image);
                        imageView.setFitWidth(200); // Max width for image
                        imageView.setPreserveRatio(true);
                        imageView.getStyleClass().add("bubble-image");
                        bubbleContent.getChildren().add(imageView);
                    } catch (IOException e) {
                        System.err.println("Error loading image: " + e.getMessage());
                        // Fallback to text if image loading fails
                        TextFlow fallbackFlow = new TextFlow(new Text("Image: " + content));
                        bubbleContent.getChildren().add(fallbackFlow);
                    }
                } else {
                    TextFlow fallbackFlow = new TextFlow(new Text("Image: " + content));
                    bubbleContent.getChildren().add(fallbackFlow);
                }
                break;
            case FILE:
                if (fileContent != null && fileContent.length > 0) {
                    HBox fileDisplay = new HBox(5);
                    fileDisplay.setAlignment(Pos.CENTER_LEFT);
                    fileDisplay.getStyleClass().add("bubble-file-display");

                    // Corrected path for file_icon.png
                    ImageView fileIcon = new ImageView(new Image(getClass().getResourceAsStream("/com/chat/ui/file_icon.png")));
                    fileIcon.setFitWidth(24);
                    fileIcon.setFitHeight(24);

                    VBox fileInfo = new VBox(2);
                    Label fileNameLabel = new Label(content); // content holds the file name
                    fileNameLabel.getStyleClass().add("bubble-file-name");
                    fileNameLabel.setWrapText(true);
                    fileNameLabel.setMaxWidth(250);

                    String fileSize = formatFileSize(fileContent.length);
                    Label fileSizeLabel = new Label(fileSize);
                    fileSizeLabel.getStyleClass().add("bubble-file-size");

                    fileInfo.getChildren().addAll(fileNameLabel, fileSizeLabel);
                    fileDisplay.getChildren().addAll(fileIcon, fileInfo);
                    bubbleContent.getChildren().add(fileDisplay);

                    // Add Open and Save as... buttons for files
                    HBox fileActions = new HBox(8);
                    fileActions.setAlignment(Pos.CENTER_LEFT);
                    fileActions.getStyleClass().add("file-actions");

                    Button openButton = new Button("Open");
                    openButton.getStyleClass().add("file-action-button");
                    openButton.setOnAction(e -> {
                        // In a real application, you'd open the file with the default system application
                        // For now, we'll just print a message.
                        System.out.println("Attempting to open file: " + content);
                        // You might save it to a temp file and then open it
                        try {
                            File tempFile = File.createTempFile("whatsapp_temp_", "." + fileType);
                            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                                fos.write(fileContent);
                            }
                            // For desktop applications, use Desktop API to open
                            if (java.awt.Desktop.isDesktopSupported()) {
                                java.awt.Desktop.getDesktop().open(tempFile);
                            } else {
                                System.err.println("Desktop not supported, cannot open file.");
                            }
                        } catch (IOException ex) {
                            System.err.println("Error opening file: " + ex.getMessage());
                        }
                    });

                    Button saveAsButton = new Button("Save as...");
                    saveAsButton.getStyleClass().add("file-action-button");
                    saveAsButton.setOnAction(e -> {
                        FileChooser fileChooser = new FileChooser();
                        fileChooser.setTitle("Save File");
                        fileChooser.setInitialFileName(content);
                        File savedFile = fileChooser.showSaveDialog(null); // Pass primary stage if available
                        if (savedFile != null) {
                            try (FileOutputStream fos = new FileOutputStream(savedFile)) {
                                fos.write(fileContent);
                                System.out.println("File saved to: " + savedFile.getAbsolutePath());
                            } catch (IOException ex) {
                                System.err.println("Error saving file: " + ex.getMessage());
                            }
                        }
                    });

                    fileActions.getChildren().addAll(openButton, saveAsButton);
                    bubbleContent.getChildren().add(fileActions);

                } else {
                    TextFlow fallbackFlow = new TextFlow(new Text("File: " + content));
                    bubbleContent.getChildren().add(fallbackFlow);
                }
                break;
            case VOICE:
                // Voice message handling is primarily done in ChatArea's createVoiceMessageBubble
                // This part of MessageBubble constructor should ideally not be reached for VOICE type
                // But as a fallback:
                TextFlow voiceFallbackFlow = new TextFlow(new Text("Voice Message"));
                bubbleContent.getChildren().add(voiceFallbackFlow);
                break;
            case EMOJI:
                // Assuming emoji is part of content for now
                TextFlow emojiFlow = new TextFlow(new Text(content));
                bubbleContent.getChildren().add(emojiFlow);
                break;
            default:
                // Fallback for any other unhandled type
                TextFlow fallbackFlow = new TextFlow(new Text(content));
                bubbleContent.getChildren().add(fallbackFlow);
                break;
        }

        // Timestamp and Read Receipt
        HBox metaBox = new HBox(4);
        metaBox.setAlignment(Pos.CENTER_RIGHT);
        metaBox.getStyleClass().add("bubble-meta");

        timestampLabel = new Label(timestamp.format(TIME_FORMATTER));
        timestampLabel.getStyleClass().add("message-timestamp");
        metaBox.getChildren().add(timestampLabel);

        if (isSent) {
            readReceiptLabel = new Label(isRead ? "✓✓" : "✓");
            readReceiptLabel.getStyleClass().add("read-receipt");
            metaBox.getChildren().add(readReceiptLabel);
        }

        bubbleContent.getChildren().add(metaBox);

        // Reactions
        if (reactions != null && !reactions.isEmpty()) {
            reactionsRow = new HBox(4);
            reactionsRow.getStyleClass().add("bubble-reactions");
            for (String reaction : reactions) {
                Label reactionLabel = new Label(reaction);
                reactionLabel.getStyleClass().add("bubble-reaction-item");
                reactionsRow.getChildren().add(reactionLabel);
            }
            bubbleContent.getChildren().add(reactionsRow);
        }

        messageBox.getChildren().add(bubbleContent);
        HBox.setHgrow(bubbleContent, Priority.ALWAYS);
        this.getChildren().add(messageBox);

        // Add fade/slide-in animation
        FadeTransition fade = new FadeTransition(Duration.millis(250), this);
        fade.setFromValue(0);
        fade.setToValue(1);
        TranslateTransition slide = new TranslateTransition(Duration.millis(250), this);
        slide.setFromX(isSent ? 40 : -40);
        slide.setToX(0);
        fade.play();
        slide.play();
    }

    public static MessageBubble createIncomingMessage(String content) {
        return new MessageBubble(null, content, LocalDateTime.now(), false, false, MessageType.TEXT, null, null, null, false);
    }

    public static MessageBubble createOutgoingMessage(String content) {
        return new MessageBubble(null, content, LocalDateTime.now(), true, true, MessageType.TEXT, null, null, null, false);
    }

    public static MessageBubble create(String content, boolean isSent, boolean isFirst, boolean isLast, MessageType type, boolean isRead, String avatarImage, String initials, String replyPreview, String forwardedFrom, List<String> reactions, boolean isGroupChat) {
        return new MessageBubble(null, content, LocalDateTime.now(), isSent, isRead, type, replyPreview, forwardedFrom, reactions, isGroupChat);
    }

    public String getContent() {
        return content;
    }

    public MessageType getType() {
        return type;
    }

    // Helper method to format file size
    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.1f MB", bytes / (1024.0 * 1024));
        }
    }
}
