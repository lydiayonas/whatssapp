package com.chat.ui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.util.Callback;

public class ChatListView extends VBox {
    private ListView<ChatItemData> chatList;
    private List<ChatItemData> chats;
    private Consumer<ChatItemData> onChatClick;
    private Consumer<ChatItemData> onGroupProfileClick;
    private final ObjectProperty<ChatItemData> selectedChat = new SimpleObjectProperty<>(this, "selectedChat", null);
    
    public ReadOnlyObjectProperty<ChatItemData> selectedChatProperty() {
        return selectedChat;
    }

    public ChatItemData getSelectedChat() {
        return selectedChat.get();
    }

    public ChatListView() {
        getStyleClass().add("chat-list");
        initializeComponents();
    }
    
    private void initializeComponents() {
        chatList = new ListView<>();
        chatList.getStyleClass().add("chat-list-view");
        chatList.setCellFactory(new Callback<ListView<ChatItemData>, ListCell<ChatItemData>>() {
            @Override
            public ListCell<ChatItemData> call(ListView<ChatItemData> listView) {
                ChatListCell cell = new ChatListCell(onGroupProfileClick);
                cell.setOnMouseClicked(event -> {
                    if (!cell.isEmpty()) {
                        ChatItemData clickedChat = cell.getItem();
                        selectedChat.set(clickedChat);
                        if (onChatClick != null) {
                            onChatClick.accept(clickedChat);
                        }
                    }
                });
                return cell;
            }
        });
        getChildren().add(chatList);
        VBox.setVgrow(chatList, Priority.ALWAYS);
    }

    public void setChatItems(ObservableList<ChatItemData> items) {
        chatList.setItems(items);
    }
    
    public void setOnChatClick(Consumer<ChatItemData> handler) {
        this.onChatClick = handler;
    }

    public void setOnGroupProfileClick(Consumer<ChatItemData> handler) {
        this.onGroupProfileClick = handler;
    }

    // Public getter for the internal ListView
    public ListView<ChatItemData> getChatList() {
        return chatList;
    }

    private static class ChatListCell extends ListCell<ChatItemData> {
        private HBox contentBox;
        private ImageView avatar;
        private Label nameLabel;
        private Label lastMessageLabel;
        private Label timeLabel;
        private Label unreadLabel;
        private VBox infoBox;
        private VBox metaBox;
        private static final String GROUP_ICON_PATH = "/com/chat/ui/group_icon.png";
        private Consumer<ChatItemData> onGroupProfileClick;

        public ChatListCell(Consumer<ChatItemData> onGroupProfileClick) {
            super();
            this.onGroupProfileClick = onGroupProfileClick;
            contentBox = new HBox(10);
            contentBox.setAlignment(Pos.CENTER_LEFT);
            contentBox.setPadding(new Insets(8, 16, 8, 16));
            contentBox.getStyleClass().add("chat-list-item");

            avatar = new ImageView();
            avatar.setFitWidth(40);
            avatar.setFitHeight(40);
            avatar.getStyleClass().add("chat-list-avatar");

            infoBox = new VBox(2);
            nameLabel = new Label();
            nameLabel.getStyleClass().add("chat-list-name");
            lastMessageLabel = new Label();
            lastMessageLabel.getStyleClass().add("chat-list-last-message");
            infoBox.getChildren().addAll(nameLabel, lastMessageLabel);
            HBox.setHgrow(infoBox, Priority.ALWAYS);

            metaBox = new VBox();
            metaBox.getStyleClass().add("chat-list-meta-box");

            timeLabel = new Label();
            timeLabel.getStyleClass().add("chat-list-time");

            unreadLabel = new Label();
            unreadLabel.getStyleClass().add("chat-list-unread");
            unreadLabel.setAlignment(Pos.CENTER);

            metaBox.getChildren().addAll(timeLabel, unreadLabel);
            metaBox.setAlignment(Pos.TOP_RIGHT);
            VBox.setVgrow(metaBox, Priority.NEVER);

            contentBox.getChildren().addAll(avatar, infoBox, metaBox);
            setGraphic(contentBox);
        }

        private Node createGroupAvatar(String groupName) {
            // Create a container for the group avatar
            StackPane groupAvatar = new StackPane();
            groupAvatar.setPrefSize(40, 40);
            groupAvatar.getStyleClass().add("chat-list-avatar");
            groupAvatar.getStyleClass().add("group-avatar");

            // Add a background circle to ensure consistent shape
            Circle background = new Circle(20);
            background.setFill(Color.web("#E8F5FE")); // Light blue background

            // Create a group icon (👥) or initials
            Text groupIcon = new Text("👥");
            groupIcon.setFont(Font.font("Segoe UI Emoji", FontWeight.BOLD, 20));
            groupIcon.setTranslateY(2); // Slight vertical adjustment

            // Add all elements to the group avatar
            groupAvatar.getChildren().addAll(background, groupIcon);

            // Add a click handler to the group avatar
            groupAvatar.setOnMouseClicked(event -> {
                if (getItem() != null && getItem().isGroupChat) {
                    // Call the onGroupProfileClick handler passed to the cell
                    if (this.onGroupProfileClick != null) {
                        this.onGroupProfileClick.accept(getItem());
                    }
                }
            });

            return groupAvatar;
        }

        @Override
        protected void updateItem(ChatItemData chat, boolean empty) {
            super.updateItem(chat, empty);
            if (empty || chat == null) {
                setGraphic(null);
                getStyleClass().remove("chat-list-item-selected");
                contentBox.getStyleClass().removeAll("chat-list-item", "group-chat-item");
            } else {
                // Update UI elements with data from ChatItemData
                nameLabel.setText(chat.name);
                lastMessageLabel.setText(chat.getLastMessage());
                timeLabel.setText(chat.getTimestamp());

                // Handle avatar based on chat type
                if (chat.isGroupChat) {
                    // Replace the ImageView with our custom group avatar
                    Node groupAvatar = createGroupAvatar(chat.name);
                    contentBox.getChildren().set(0, groupAvatar); // Replace the avatar at index 0
                } else {
                    // Individual chat avatar logic
                    if (chat.avatarPath != null && !chat.avatarPath.isEmpty()) {
                        try {
                            Image chatAvatarImage = new Image(getClass().getResourceAsStream(chat.avatarPath));
                            if (chatAvatarImage.isError()) {
                                throw new Exception("Image not found: " + chat.avatarPath);
                            }
                            avatar.setImage(chatAvatarImage);
                        } catch (Exception e) {
                            System.err.println("Error loading avatar for " + chat.name + ": " + e.getMessage());
                            // Fallback to default avatar or placeholder
                            avatar.setImage(new Image(getClass().getResourceAsStream("/com/chat/ui/default_avatar.png")));
                        }
                    } else {
                        avatar.setImage(new Image(getClass().getResourceAsStream("/com/chat/ui/default_avatar.png")));
                    }
                    // Ensure the avatar is the ImageView
                    if (!contentBox.getChildren().get(0).equals(avatar)) {
                        contentBox.getChildren().set(0, avatar);
                    }
                }

                // Update unread count bubble
                if (chat.unreadCount > 0) {
                    unreadLabel.setText(String.valueOf(chat.unreadCount));
                    unreadLabel.setVisible(true);
                    unreadLabel.setManaged(true);
                } else {
                    unreadLabel.setVisible(false);
                    unreadLabel.setManaged(false);
                }

                // Add selected style if this chat is currently selected
                if (getListView().getSelectionModel().getSelectedItem() == chat) {
                    getStyleClass().add("chat-list-item-selected");
                } else {
                    getStyleClass().remove("chat-list-item-selected");
                }

                setGraphic(contentBox);
            }
        }
    }
}
