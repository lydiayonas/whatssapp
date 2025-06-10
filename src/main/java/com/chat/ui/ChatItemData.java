package com.chat.ui;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ChatItemData {
    public final String name;
    public final String avatarPath;
    public final boolean isPinned;
    public final int unreadCount;
    private final List<ChatArea.MockMessage> messages;
    public final boolean isGroupChat;
    public final List<Integer> participantIds;
    public final List<User> members;
    public final String statusText;
    private boolean isHistoryCleared;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private boolean isOnline;

    public ChatItemData(String name, String avatarPath, boolean isPinned, int unreadCount, List<ChatArea.MockMessage> messages, boolean isGroupChat, List<Integer> participantIds, List<User> members) {
        this.name = name;
        this.avatarPath = avatarPath;
        this.isPinned = isPinned;
        this.unreadCount = unreadCount;
        this.messages = new ArrayList<>(messages != null ? messages : new ArrayList<>());
        this.isGroupChat = isGroupChat;
        this.participantIds = participantIds != null ? new ArrayList<>(participantIds) : new ArrayList<>();
        this.members = members != null ? new ArrayList<>(members) : new ArrayList<>();
        if (isGroupChat) {
            this.statusText = (members != null ? members.size() : 0) + " members";
        } else {
            this.statusText = "last seen recently";
        }
        this.isHistoryCleared = false;
    }

    public ChatItemData(String name, String avatarPath, boolean isPinned, int unreadCount, List<ChatArea.MockMessage> messages, boolean isGroupChat, List<Integer> participantIds, List<User> members, String statusText) {
        this.name = name;
        this.avatarPath = avatarPath;
        this.isPinned = isPinned;
        this.unreadCount = unreadCount;
        this.messages = new ArrayList<>(messages != null ? messages : new ArrayList<>());
        this.isGroupChat = isGroupChat;
        this.participantIds = participantIds != null ? new ArrayList<>(participantIds) : new ArrayList<>();
        this.members = members != null ? new ArrayList<>(members) : new ArrayList<>();
        this.statusText = statusText;
        this.isHistoryCleared = false;
    }

    public String getLastMessage() {
        if (messages.isEmpty()) {
            return "No messages yet";
        } else {
            ChatArea.MockMessage lastMsg = messages.get(messages.size() - 1);
            return lastMsg.content;
        }
    }

    public String getTimestamp() {
        if (messages.isEmpty()) {
            return "";
        } else {
            ChatArea.MockMessage lastMsg = messages.get(messages.size() - 1);
            return lastMsg.timestamp.format(DateTimeFormatter.ofPattern("h:mm a"));
        }
    }

    public List<ChatArea.MockMessage> getMessages() {
        return messages;
    }

    public List<User> getMembers() {
        return members;
    }

    public boolean isHistoryCleared() {
        return isHistoryCleared;
    }

    public void setHistoryCleared(boolean historyCleared) {
        isHistoryCleared = historyCleared;
    }

    public String getName() {
        return name;
    }
} 