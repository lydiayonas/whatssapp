package com.chat.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import com.chat.common.Message;
import com.chat.ui.ChatArea.MockMessage;
import com.chat.ui.MessageType;
import com.chat.ui.User;
import com.chat.ui.ChatItemData;
import com.chat.client.ChatClient;

@Component
public class MainView extends BorderPane {
    private static final String LIGHT_THEME = "/com/chat/ui/styles-light.css";
    private static final String DARK_THEME = "/com/chat/ui/styles-dark.css";
    private boolean isDark = false;
    private Scene scene;

    private Sidebar sidebarComponent;
    private ChatArea chatAreaComponent;
    private ChatItemData selectedChat;

    private enum ViewState { WELCOME, LOGIN, CODE, CHAT }
    private ViewState currentState = ViewState.WELCOME;

    private WelcomeView welcomeView;
    private VBox loginView;
    private VBox codeView;
    private TextField phoneField;
    private Button loginNext;
    private Button codeNext;

    private ComboBox<Country> countryBox;

    private Label phoneValidationLabel; // Label for phone number validation messages

    // Simple data class for Country
    private static class Country {
        String name;
        String code;

        Country(String name, String code) {
            this.name = name;
            this.code = code;
        }

        @Override
        public String toString() {
            return name + " (" + code + ")";
        }
    }

    // List of countries with codes (simplified)
    private List<Country> countries = new ArrayList<>(Arrays.asList(
        new Country("Ethiopia", "+251"),
        new Country("United States", "+1"),
        new Country("United Kingdom", "+44"),
        new Country("Canada", "+1"),
        new Country("Germany", "+49")
    ));

    private List<User> mockUsers = new ArrayList<>(); // Empty the mock users list

    private ObservableList<ChatItemData> chatListData = FXCollections.observableArrayList(); // Empty the chat data list

    private User getUserById(int id) {
        // Since mockUsers is empty, this will always return null or throw an exception if not handled
        return null;
    }
    
    private VBox leftSidebarMenu;
    private Button hamburger;
    private VBox rightSidebar;
    private boolean rightSidebarVisible = false;

    private ChatClient chatClient;

    public void start(Stage primaryStage) {
        primaryStage.setTitle("WhatsApp Desktop");
        scene = new Scene(this, 1200, 800);
        scene.getStylesheets().add(getClass().getResource(LIGHT_THEME).toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public MainView() {
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        populateMockData();
    }

    private void initializeComponents() {
        sidebarComponent = new Sidebar(chatListData, this::handleChatSelection, this::switchTheme);
        chatAreaComponent = new ChatArea();
        chatAreaComponent.setOnMessageSent(this::handleMessageSent);
        chatAreaComponent.setChatListData(chatListData);  // Set the chat list data

        // The following components are no longer used as Login and OTP views are removed
        welcomeView = new WelcomeView();
        loginView = new VBox(16);
        codeView = new VBox(16);
        phoneField = new TextField();
        loginNext = new Button();
        codeNext = new Button();
        countryBox = new ComboBox<>();
        phoneValidationLabel = new Label();
        leftSidebarMenu = new VBox();
        hamburger = new Button();
        rightSidebar = new VBox();
    }

    private void layoutComponents() {
        HBox mainContent = new HBox();
        mainContent.getChildren().addAll(sidebarComponent, chatAreaComponent);
        HBox.setHgrow(chatAreaComponent, Priority.ALWAYS);
        setCenter(mainContent);
    }

    private void setupEventHandlers() {
        sidebarComponent.setOnNewChatCreated(this::handleNewChatCreated);
    }

    private void handleChatSelection(ChatItemData selectedChat) {
        this.selectedChat = selectedChat;
        if (selectedChat != null) {
            System.out.println("Selected chat: " + selectedChat.name + ", Messages count: " + selectedChat.getMessages().size());
            chatAreaComponent.setChatHeader(selectedChat.name, selectedChat.statusText, selectedChat.avatarPath);
            chatAreaComponent.setGroupChat(selectedChat.isGroupChat);
            chatAreaComponent.setCurrentChat(selectedChat);  // Set the current chat first
            chatAreaComponent.loadConversation(selectedChat.getMessages());
            chatAreaComponent.setCurrentUser(new User(1, "abebe", "/com/chat/ui/default_avatar.png", true)); // Assuming abebe is current user, adjust as needed
            chatAreaComponent.setVisible(true); // Ensure ChatArea is visible
            chatAreaComponent.setManaged(true); // Ensure ChatArea takes up layout space
        } else {
            chatAreaComponent.setCurrentChat(null);  // Clear the current chat
        }
    }

    private String getLastMessage(ChatItemData chatItem) {
        if (chatItem == null || chatItem.getMessages().isEmpty()) {
            return "";
        } else {
            return chatItem.getMessages().get(chatItem.getMessages().size() - 1).content;
        }
    }

    private List<ChatArea.MockMessage> createMockConversation(ChatItemData chatItem) {
        if (chatItem == null || chatItem.isHistoryCleared()) {
            return new ArrayList<>();
        }
        return chatItem.getMessages();
    }

    private void populateMockData() {
        // Mock Users (using the correct constructor for com.chat.ui.User)
        User userMaya = new User(1, "Maya Kasuma", "/com/chat/ui/default_avatar.png", true);
        User userJason = new User(2, "Jason Ballmer", "/com/chat/ui/default_avatar.png", true);
        User userAlice = new User(3, "Alice Whitman", "/com/chat/ui/default_avatar.png", true);
        User userRebecca = new User(4, "Rebecca", "/com/chat/ui/default_avatar.png", false);
        User userStasa = new User(5, "Stasa Benko", "/com/chat/ui/default_avatar.png", false);
        User userMark = new User(6, "Mark Rogers", "/com/chat/ui/default_avatar.png", false);
        User userDawn = new User(7, "Dawn Jones", "/com/chat/ui/default_avatar.png", true);
        User userZiggy = new User(8, "Ziggy Woodrow", "/com/chat/ui/default_avatar.png", false);

        // Additional users for group chats
        User userChrisR = new User(9, "Chris R", "/com/chat/ui/default_avatar.png", true);
        User userAnnaB = new User(10, "Anna B", "/com/chat/ui/default_avatar.png", false);
        User userDad = new User(11, "Dad", "/com/chat/ui/default_avatar.png", false);
        User userMom = new User(12, "Mom", "/com/chat/ui/default_avatar.png", true);
        User userSister = new User(13, "Sister", "/com/chat/ui/default_avatar.png", true);
        User userBrother = new User(14, "Brother", "/com/chat/ui/default_avatar.png", false);

        // Mock Messages
        List<MockMessage> mayaMessages = Arrays.asList(
            new MockMessage(userMaya, "Yes! OK", LocalDateTime.now().minusMinutes(5), MessageType.TEXT, null, null, null, false, true, null, null)
        );

        List<MockMessage> jasonMessages = Arrays.asList(
            new MockMessage(userJason, "Video", LocalDateTime.now().minusMinutes(34), MessageType.TEXT, null, null, null, false, true, null, null)
        );

        List<MockMessage> aliceMessages = Arrays.asList(
            new MockMessage(userAlice, "Wow! Have great time. Enjoy.", LocalDateTime.now().minusMinutes(8), MessageType.TEXT, null, null, null, true, true, null, null),
            new MockMessage(userAlice, "OK! \uD83D\uDC4C", LocalDateTime.now().minusMinutes(2), MessageType.TEXT, null, null, null, false, true, null, null)
        );

        List<MockMessage> bakingClubMessages = Arrays.asList(
            new MockMessage(userRebecca, "@Chris R?", LocalDateTime.now().minusHours(1), MessageType.TEXT, null, null, null, false, false, null, null)
        );

        List<MockMessage> stasaMessages = Arrays.asList(
            new MockMessage(userStasa, "Aww no problem.", LocalDateTime.now().minusHours(2), MessageType.TEXT, null, null, null, false, true, null, null)
        );

        List<MockMessage> familyFoodiesMessages = Arrays.asList(
            new MockMessage(userAlice, "Dinner last night", LocalDateTime.now().minusHours(3), MessageType.TEXT, null, null, null, true, false, null, null)
        );

        List<MockMessage> markMessages = Arrays.asList(
            new MockMessage(userMark, "typing...", LocalDateTime.now().minusHours(4), MessageType.TEXT, null, null, null, false, false, null, null)
        );

        List<MockMessage> dawnMessages = Arrays.asList(
            new MockMessage(userDawn, "Yes that's my fave too!", LocalDateTime.now().minusHours(5), MessageType.TEXT, null, null, null, false, true, null, null)
        );

        List<MockMessage> ziggyMessages = Arrays.asList(
            new MockMessage(userZiggy, "", LocalDateTime.now().minusDays(1), MessageType.TEXT, null, null, null, false, true, null, null)
        );

        // Add ChatItemData to the observable list
        chatListData.add(new ChatItemData("Maya Kasuma", "/com/chat/ui/default_avatar.png", false, 0, mayaMessages, false, Arrays.asList(userMaya.id), Arrays.asList(userMaya), "online"));
        chatListData.add(new ChatItemData("Jason Ballmer", "/com/chat/ui/default_avatar.png", false, 3, jasonMessages, false, Arrays.asList(userJason.id), Arrays.asList(userJason), "online"));
        chatListData.add(new ChatItemData("Alice Whitman", "/com/chat/ui/default_avatar.png", false, 0, aliceMessages, false, Arrays.asList(userAlice.id), Arrays.asList(userAlice), "online"));
        // For Baking Club: Pass member IDs and actual User objects
        List<User> bakingClubMembers = Arrays.asList(userRebecca, userChrisR, userAnnaB);
        List<Integer> bakingClubMemberIds = Arrays.asList(userRebecca.id, userChrisR.id, userAnnaB.id);
        chatListData.add(new ChatItemData("Baking Club", "/com/chat/ui/default_avatar.png", false, 1, bakingClubMessages, true, bakingClubMemberIds, bakingClubMembers, bakingClubMembers.size() + " members"));

        chatListData.add(new ChatItemData("Stasa Benko", "/com/chat/ui/default_avatar.png", false, 2, stasaMessages, false, Arrays.asList(userStasa.id), Arrays.asList(userStasa), "last seen 2h ago"));

        // For Family Foodies: Pass member IDs and actual User objects
        List<User> familyFoodiesMembers = Arrays.asList(userAlice, userDad, userMom, userSister, userBrother);
        List<Integer> familyFoodiesMemberIds = Arrays.asList(userAlice.id, userDad.id, userMom.id, userSister.id, userBrother.id);
        chatListData.add(new ChatItemData("Family Foodies", "/com/chat/ui/default_avatar.png", false, 0, familyFoodiesMessages, true, familyFoodiesMemberIds, familyFoodiesMembers, familyFoodiesMembers.size() + " members"));

        chatListData.add(new ChatItemData("Mark Rogers", "/com/chat/ui/default_avatar.png", false, 0, markMessages, false, Arrays.asList(userMark.id), Arrays.asList(userMark), "typing..."));
        chatListData.add(new ChatItemData("Dawn Jones", "/com/chat/ui/default_avatar.png", false, 0, dawnMessages, false, Arrays.asList(userDawn.id), Arrays.asList(userDawn), "online"));
        chatListData.add(new ChatItemData("Ziggy Woodrow", "/com/chat/ui/default_avatar.png", false, 0, ziggyMessages, false, Arrays.asList(userZiggy.id), Arrays.asList(userZiggy), "last seen yesterday"));

        // Select the first chat by default
        if (!chatListData.isEmpty()) {
            Platform.runLater(() -> sidebarComponent.setSelectedChat(chatListData.get(0)));
        }
    }

    private void switchTheme(Boolean isDark) {
        String theme = isDark ? DARK_THEME : LIGHT_THEME;
        scene.getStylesheets().clear();
        scene.getStylesheets().add(getClass().getResource(theme).toExternalForm());
        this.isDark = isDark;
    }

    private void handleMessageSent(ChatArea.MockMessage message) {
        if (selectedChat != null) {
            selectedChat.getMessages().add(message);
            // Update the chat list item to reflect the new last message and timestamp
            int index = chatListData.indexOf(selectedChat);
            if (index != -1) {
                chatListData.set(index, selectedChat); // This will trigger update in ChatListView
            }
        }
    }

    private void handleNewChatCreated(ChatItemData newChat) {
        // The new chat is already added to chatListData in Sidebar.java
        // Now ensure it's selected and conversation is loaded
        System.out.println("New chat created in sidebar: " + newChat.name);
        handleChatSelection(newChat);
    }

    public ChatClient getChatClient() {
        return chatClient;
    }
}
