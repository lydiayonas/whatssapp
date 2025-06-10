package com.chat.controller;

import com.chat.model.GroupChat;
import com.chat.model.Message;
import com.chat.model.User;
import com.chat.repository.GroupChatRepository;
import com.chat.repository.MessageRepository;
import com.chat.repository.UserRepository;
import com.chat.service.GroupChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class GroupMessageController {
    private final GroupChatService groupChatService;
    private final MessageRepository messageRepository;
    private final GroupChatRepository groupChatRepository;
    private final UserRepository userRepository;

    @Autowired
    public GroupMessageController(GroupChatService groupChatService,
                                  MessageRepository messageRepository,
                                  GroupChatRepository groupChatRepository,
                                  UserRepository userRepository) {
        this.groupChatService = groupChatService;
        this.messageRepository = messageRepository;
        this.groupChatRepository = groupChatRepository;
        this.userRepository = userRepository;
    }

    @MessageMapping("/group/{groupId}/send")
    @SendTo("/topic/group.{groupId}")
    public Message handleGroupMessage(
            @DestinationVariable Long groupId,
            Message message) {
        // Attach group and sender
        GroupChat group = groupChatRepository.findById(groupId).orElse(null);
        if (group != null) {
            message.setGroupChat(group);
        }
        if (message.getSender() != null) {
            User sender = userRepository.findById(message.getSender().getId()).orElse(null);
            if (sender != null) {
                message.setSender(sender);
            }
        }
        // Save message (including file/voice)
        Message saved = messageRepository.save(message);
        return saved;
    }
} 