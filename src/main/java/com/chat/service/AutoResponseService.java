package com.chat.service;

import com.chat.common.Message;
import com.chat.model.User;
import com.chat.model.Chat;
import org.springframework.stereotype.Service;
import java.util.Random;

@Service
public class AutoResponseService {
    private final Random random = new Random();
    
    private final String[] responses = {
        "I'm here to help! What can I do for you?",
        "That's interesting! Tell me more about it.",
        "I understand. How can I assist you further?",
        "Thanks for sharing that with me!",
        "I'm processing your message. What else would you like to know?",
        "That's a great point! Let me think about it.",
        "I'm here to chat! What's on your mind?",
        "Interesting perspective! Would you like to elaborate?",
        "I'm listening! Please continue.",
        "That's fascinating! Tell me more."
    };

    public Message generateResponse(Message originalMessage, User botUser, Chat chat) {
        String responseContent = responses[random.nextInt(responses.length)];
        
        return new Message(
            "Bot",
            originalMessage.getSender(),
            responseContent,
            Message.MessageType.CHAT
        );
    }
} 