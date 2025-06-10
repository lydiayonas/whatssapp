package com.chat.controller;

import com.chat.dto.GroupChatDTO;
import com.chat.model.GroupChat;
import com.chat.service.GroupChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/groups")
public class GroupChatController {
    private final GroupChatService groupChatService;

    @Autowired
    public GroupChatController(GroupChatService groupChatService) {
        this.groupChatService = groupChatService;
    }

    @PostMapping
    public ResponseEntity<GroupChat> createGroup(
            @RequestBody GroupChatDTO groupDTO,
            @RequestParam Long adminId) {
        GroupChat group = groupChatService.createGroup(groupDTO, adminId);
        return ResponseEntity.ok(group);
    }

    @PostMapping("/{groupId}/members/{userId}")
    public ResponseEntity<Void> addMember(
            @PathVariable Long groupId,
            @PathVariable Long userId) {
        groupChatService.addMember(groupId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long groupId,
            @PathVariable Long userId) {
        groupChatService.removeMember(groupId, userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<Void> updateGroup(
            @PathVariable Long groupId,
            @RequestBody GroupChatDTO groupDTO) {
        groupChatService.updateGroup(groupId, groupDTO);
        return ResponseEntity.ok().build();
    }
} 