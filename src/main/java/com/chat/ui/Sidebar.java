package com.chat.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
import javafx.scene.control.Alert;
import java.util.Optional;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.collections.ListChangeListener;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;

import java.util.List;
import java.util.function.Consumer;
import java.time.LocalDateTime;
import java.util.ArrayList;

import com.chat.ui.ChatItemData;
import com.chat.ui.ChatArea.MockMessage;
import com.chat.ui.MessageType;
import com.chat.common.DatabaseUtil;

public class Sidebar extends VBox {

    private ImageView userAvatar;
    private Button newChatButton;
    private Button createGroupButton;
    private Button moreOptionsButton;
    private Button topBarSearchButton;
    private Button searchCloseButton;
    private HBox topBar; // Declare topBar as a class member

    private ChatListView chatListView;
    private TextField searchField;
    private HBox searchBarContainer; // Container for search field and close button
    private boolean isSearchVisible = false;
    private FilteredList<ChatItemData> filteredChatList;
    private ObservableList<ChatItemData> chatListData;

    private Consumer<ChatItemData> onChatSelected;
    private Consumer<Boolean> onThemeToggle;
    private Consumer<ChatItemData> onGroupProfileClick;
    private Consumer<ChatItemData> onNewChatCreated;
    private Button themeToggleButton;
    private User currentUser;

    public Sidebar(ObservableList<ChatItemData> chatListData, Consumer<ChatItemData> onChatSelected, Consumer<Boolean> onThemeToggle) {
        this.chatListData = chatListData;
        this.onChatSelected = onChatSelected;
        this.onThemeToggle = onThemeToggle;
        getStyleClass().add("sidebar");
        setMinWidth(280);
        setPrefWidth(280);
        setMaxWidth(350);
        setPadding(new Insets(0));
        setSpacing(0);

        initializeComponents();
        layoutComponents();
        setupEventHandlers();

        // Set up the initial chat list and selection listener after components are initialized
        filteredChatList = new FilteredList<>(this.chatListData);
        chatListView.setChatItems(filteredChatList); // Use setChatItems

        // Listen for chat selection changes in ChatListView and propagate them
        chatListView.selectedChatProperty().addListener((obs, oldChat, newChat) -> {
            if (newChat != null && onChatSelected != null) {
                onChatSelected.accept(newChat);
            }
        });

        // Listen for changes in chatListData to update the filtered list
        this.chatListData.addListener((ListChangeListener<ChatItemData>) c -> {
            while (c.next()) {
                if (c.wasAdded() || c.wasRemoved() || c.wasUpdated()) {
                    filteredChatList.setPredicate(filteredChatList.getPredicate()); // Reapply predicate
                }
            }
        });
    }

    private void initializeComponents() {
        // User Avatar
        userAvatar = new ImageView(new Image(getClass().getResourceAsStream("/com/chat/ui/default_avatar.png"))); // Placeholder
        userAvatar.setFitWidth(40);
        userAvatar.setFitHeight(40);
        userAvatar.getStyleClass().add("sidebar-avatar");

        Label chatsTitle = new Label("Chats");
        chatsTitle.getStyleClass().add("sidebar-chats-title");

        Region spacerTop = new Region();
        HBox.setHgrow(spacerTop, Priority.ALWAYS);

        // Remove direct buttons and create menu items for them
        // New Chat Button (now a menu item)
        // createGroupButton (now a menu item)

        // More Options Button to hold the dropdown menu
        moreOptionsButton = new Button("⋯"); // Using ellipsis
        moreOptionsButton.getStyleClass().addAll("icon-button", "large-icon-button");

        // Create ContextMenu for moreOptionsButton
        ContextMenu optionsMenu = new ContextMenu();

        MenuItem newChatMenuItem = new MenuItem("New Chat");
        newChatMenuItem.setOnAction(e -> showNewChatDialog());

        MenuItem createGroupMenuItem = new MenuItem("Create New Group");
        createGroupMenuItem.setOnAction(e -> showCreateGroupDialog());

        optionsMenu.getItems().addAll(newChatMenuItem, createGroupMenuItem);

        moreOptionsButton.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) { // Only show on left-click
                optionsMenu.show(moreOptionsButton, event.getScreenX(), event.getScreenY());
            }
        });

        topBar = new HBox(12); // Initialize topBar here
        topBar.getStyleClass().add("sidebar-topbar");
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10, 16, 10, 16));
        topBar.getChildren().addAll(userAvatar, chatsTitle, spacerTop, moreOptionsButton); // Only moreOptionsButton is here now

        // Search field with icon inside
        searchField = new TextField();
        searchField.getStyleClass().add("search-box");
        searchField.setPromptText("Search or start new chat");
        searchField.setPrefHeight(32);
        searchField.setManaged(true);
        searchField.setVisible(true);

        searchCloseButton = new Button("✖");
        searchCloseButton.getStyleClass().add("icon-button");
        searchCloseButton.setVisible(false);
        searchCloseButton.setManaged(false);

        searchBarContainer = new HBox(searchField, searchCloseButton);
        searchBarContainer.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchBarContainer.setPadding(new Insets(8, 16, 8, 16));
        searchBarContainer.getStyleClass().add("sidebar-search-container");

        chatListView = new ChatListView();
        VBox.setVgrow(chatListView, Priority.ALWAYS);
    }

    private void layoutComponents() {
        // Wrap the content in a StackPane to layer the button on top
        StackPane contentStack = new StackPane();

        // Use the initialized topBar field directly
        VBox mainContent = new VBox(topBar, searchBarContainer, chatListView);
        VBox.setVgrow(chatListView, Priority.ALWAYS); // Ensure chatListView expands

        contentStack.getChildren().addAll(mainContent);

        // Set the StackPane as the single child of the Sidebar VBox
        this.getChildren().clear();
        this.getChildren().add(contentStack);
        VBox.setVgrow(contentStack, Priority.ALWAYS);
    }

    private void setupEventHandlers() {
        // Handlers for new menu items
        // newChatButton.setOnAction(e -> showNewChatDialog()); // Removed, now handled by menu item
        // createGroupButton.setOnAction(e -> showCreateGroupDialog()); // Removed, now handled by menu item
        // moreOptionsButton.setOnAction(e -> System.out.println("More options clicked!")); // Original placeholder, replaced by context menu
        searchCloseButton.setOnAction(e -> hideSearch());

        // Filter the chat list when the search text changes
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredChatList.setPredicate(chat -> {
                if (newVal == null || newVal.isEmpty()) {
                    searchCloseButton.setVisible(false);
                    searchCloseButton.setManaged(false);
                    return true;
                }
                searchCloseButton.setVisible(true);
                searchCloseButton.setManaged(true);
                return chat.name.toLowerCase().contains(newVal.toLowerCase());
            });
        });
    }

    private void toggleSearch() {
        isSearchVisible = !isSearchVisible;
        // The search bar is always visible now, but its styling might change based on this state
        // For now, we'll just toggle the close button
        if (isSearchVisible) {
            searchField.requestFocus();
            searchCloseButton.setVisible(true);
            searchCloseButton.setManaged(true);
        } else {
            searchField.clear();
            searchCloseButton.setVisible(false);
            searchCloseButton.setManaged(false);
        }
    }

    private void hideSearch() {
        isSearchVisible = false;
        searchField.clear();
        searchCloseButton.setVisible(false);
        searchCloseButton.setManaged(false);
    }

    // This method is no longer needed as topBar is a class member
    // private HBox getTopBar() {
    //     return topBar;
    // }

    public Button getHamburgerButton() {
        return null; // Hamburger button removed
    }

    public Button getThemeToggleButton() {
        return themeToggleButton; // This will be null unless set elsewhere
    }

    // Method to update the chat list displayed in the ChatListView
    public void updateChatList(ObservableList<ChatItemData> updatedList) {
        this.chatListData = updatedList;
        // Update the source of the filtered list to the new list
        filteredChatList = new FilteredList<>(this.chatListData, filteredChatList.getPredicate());
        chatListView.setChatItems(filteredChatList);
    }

    // Public method to get the currently selected chat
    public ReadOnlyObjectProperty<ChatItemData> selectedChatProperty() {
        return chatListView.selectedChatProperty();
    }

    public void setOnThemeToggle(Consumer<Boolean> onThemeToggle) {
        this.onThemeToggle = onThemeToggle;
    }

    public void setOnGroupProfileClick(Consumer<ChatItemData> onGroupProfileClick) {
        this.onGroupProfileClick = onGroupProfileClick;
    }

    // Public method to set the new chat created callback
    public void setOnNewChatCreated(Consumer<ChatItemData> onNewChatCreated) {
        this.onNewChatCreated = onNewChatCreated;
    }

    // Public method to programmatically select a chat
    public void setSelectedChat(ChatItemData chatItem) {
        if (chatListView != null && chatListView.getChatList() != null) {
            chatListView.getChatList().getSelectionModel().select(chatItem);
        }
    }

    // Method to show the new chat dialog (to be implemented next)
    private void showNewChatDialog() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Create New Chat");
        dialog.setHeaderText("Enter username to start a new chat:");

        // Set the button types
        ButtonType createButtonType = new ButtonType("Create", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Create the username field
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        grid.addRow(0, new Label("Username:"), usernameField);

        dialog.getDialogPane().setContent(grid);

        // Request focus on the username field by default
        Platform.runLater(usernameField::requestFocus);

        // Convert the result to a username-password pair when the create button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return new Pair<>(usernameField.getText(), null);
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(usernamePair -> {
            String newChatUsername = usernamePair.getKey();
            if (newChatUsername != null && !newChatUsername.trim().isEmpty()) {
                if (DatabaseUtil.userExists(newChatUsername)) {
                    // Check if a chat with this user already exists
                    boolean chatExists = false;
                    for (ChatItemData chat : chatListData) {
                        if (!chat.isGroupChat && chat.name.equalsIgnoreCase(newChatUsername)) {
                            chatExists = true;
                            break;
                        }
                    }

                    if (!chatExists) {
                        // Create a new ChatItemData for the new chat
                        ChatItemData newChat = new ChatItemData(
                            newChatUsername,
                            "/com/chat/ui/default_avatar.png", // Corrected default avatar path
                            false, // Not pinned
                            0,     // No unread messages
                            new ArrayList<MockMessage>(), // Empty message list
                            false, // Not a group chat
                            null,  // No participant IDs (for single chat)
                            new ArrayList<User>() // No members (for single chat)
                        );
                        // Add the current user to the chat members
                        if (currentUser != null) {
                            newChat.getMembers().add(currentUser);
                        }
                        // Add the new contact to the chat members
                        newChat.getMembers().add(new User(0, newChatUsername, "/com/chat/ui/default_avatar.png", false)); // Corrected default avatar path

                        chatListData.add(0, newChat); // Add to the top of the list
                        setSelectedChat(newChat); // Select the new chat
                if (onNewChatCreated != null) {
                    onNewChatCreated.accept(newChat);
                        }
                    } else {
                        showError("A chat with '" + newChatUsername + "' already exists.");
                    }
                } else {
                    showError("User '" + newChatUsername + "' does not exist. Please check the username.");
                }
            }
        });
    }

    private void showCreateGroupDialog() {
        Dialog<Pair<String, List<User>>> dialog = new Dialog<>();
        dialog.setTitle("Create New Group");
        dialog.setHeaderText("Enter group name and select members:");

        ButtonType createButtonType = new ButtonType("Create", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        TextField groupNameField = new TextField();
        groupNameField.setPromptText("Group Name");

        ListView<User> userSelectionList = new ListView<>();
        userSelectionList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        userSelectionList.setCellFactory(param -> new javafx.scene.control.ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                } else {
                    setText(user.name);
                }
            }
        });

        // Populate userSelectionList with existing users from chatListData
        ObservableList<User> availableUsers = FXCollections.observableArrayList();
        for (ChatItemData chat : chatListData) {
            if (!chat.isGroupChat && chat.getMembers() != null && !chat.getMembers().isEmpty()) {
                // For direct chats, the other member is the contact
                User contact = chat.getMembers().stream()
                                  .filter(member -> currentUser == null || member.id != currentUser.id) // Exclude current user if available
                                  .findFirst().orElse(null);
                if (contact != null && !availableUsers.contains(contact)) {
                    availableUsers.add(contact);
                }
            } else if (chat.isGroupChat && chat.getMembers() != null) {
                // For group chats, add all members (excluding current user)
                for (User member : chat.getMembers()) {
                    if (currentUser == null || member.id != currentUser.id) {
                        if (!availableUsers.contains(member)) {
                            availableUsers.add(member);
                        }
                    }
                }
            }
        }

        // Add current user to available users if not already present
        if (currentUser != null && !availableUsers.contains(currentUser)) {
            availableUsers.add(currentUser);
        }

        userSelectionList.setItems(availableUsers);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20, 150, 10, 10));
        content.getChildren().addAll(
            new Label("Group Name:"),
            groupNameField,
            new Label("Select Members:"),
            userSelectionList
        );

        dialog.getDialogPane().setContent(content);

        Platform.runLater(groupNameField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                String groupName = groupNameField.getText().trim();
                List<User> selectedMembers = new ArrayList<>(userSelectionList.getSelectionModel().getSelectedItems());
                if (currentUser != null && !selectedMembers.contains(currentUser)) {
                    selectedMembers.add(currentUser); // Add current user to group members
                }
                return new Pair<>(groupName, selectedMembers);
            }
            return null;
        });

        Optional<Pair<String, List<User>>> result = dialog.showAndWait();

        result.ifPresent(groupData -> {
            String groupName = groupData.getKey();
            List<User> selectedMembers = groupData.getValue();

            if (groupName != null && !groupName.isEmpty() && !selectedMembers.isEmpty()) {
                // Create a new ChatItemData for the group chat
                ChatItemData newGroupChat = new ChatItemData(
                    groupName,
                    "/com/chat/ui/default_avatar.png", // Corrected default avatar for groups
                    false, // Not pinned
                    0,     // No unread messages
                    new ArrayList<MockMessage>(), // Empty message list
                    true,  // Is a group chat
                    null,  // No participant IDs (we'll use members directly)
                    selectedMembers // Selected members
                );

                chatListData.add(0, newGroupChat); // Add to the top of the list
                setSelectedChat(newGroupChat); // Select the new group chat
                if (onNewChatCreated != null) {
                    onNewChatCreated.accept(newGroupChat);
                }
            } else {
                showError("Group name cannot be empty and at least one member must be selected.");
            }
        });
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 