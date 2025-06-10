package com.chat.controller;

import com.chat.model.Chat;
import com.chat.model.GroupChat;
import com.chat.service.ChatListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chatlist")
public class ChatListController {
    private final ChatListService chatListService;

    @Autowired
    public ChatListController(ChatListService chatListService) {
        this.chatListService = chatListService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getUserChatList(@PathVariable Long userId) {
        List<Chat> chats = chatListService.getUserChats(userId);
        List<GroupChat> groups = chatListService.getUserGroups(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("chats", chats);
        result.put("groups", groups);
        return ResponseEntity.ok(result);
    }
} 