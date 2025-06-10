package com.chat.service;

import com.chat.model.Chat;
import com.chat.model.GroupChat;
import com.chat.model.User;
import com.chat.repository.ChatRepository;
import com.chat.repository.GroupChatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatListService {
    private final ChatRepository chatRepository;
    private final GroupChatRepository groupChatRepository;

    @Autowired
    public ChatListService(ChatRepository chatRepository, GroupChatRepository groupChatRepository) {
        this.chatRepository = chatRepository;
        this.groupChatRepository = groupChatRepository;
    }

    public List<Chat> getUserChats(Long userId) {
        return chatRepository.findByParticipantsId(userId);
    }

    public List<GroupChat> getUserGroups(Long userId) {
        return groupChatRepository.findByMembersId(userId);
    }

    public Chat createDirectChat(User user1, User user2) {
        // Check if chat already exists
        List<Chat> existingChats = chatRepository.findByParticipantsId(user1.getId());
        for (Chat chat : existingChats) {
            if (chat.getParticipants().contains(user2)) {
                return chat;
            }
        }

        // Create new chat
        Chat chat = new Chat();
        chat.setName(user2.getUsername()); // Default name is the other user's username
        chat.getParticipants().add(user1);
        chat.getParticipants().add(user2);
        return chatRepository.save(chat);
    }
} 