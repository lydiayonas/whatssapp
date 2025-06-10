package com.chat.service;

import com.chat.dto.GroupChatDTO;
import com.chat.model.GroupChat;
import com.chat.model.User;
import com.chat.repository.GroupChatRepository;
import com.chat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class GroupChatService {
    private final GroupChatRepository groupChatRepository;
    private final UserRepository userRepository;

    @Autowired
    public GroupChatService(GroupChatRepository groupChatRepository, UserRepository userRepository) {
        this.groupChatRepository = groupChatRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public GroupChat createGroup(GroupChatDTO groupDTO, Long adminId) {
        User admin = userRepository.findById(adminId)
            .orElseThrow(() -> new RuntimeException("Admin not found"));

        GroupChat group = new GroupChat();
        group.setName(groupDTO.getName());
        group.setDescription(groupDTO.getDescription());
        group.setAdmin(admin);
        group.setPrivate(groupDTO.isPrivate());

        // Add admin to members
        Set<User> members = new HashSet<>();
        members.add(admin);

        // Add other members
        if (groupDTO.getMemberIds() != null) {
            groupDTO.getMemberIds().forEach(memberId -> {
                userRepository.findById(memberId).ifPresent(members::add);
            });
        }

        group.setMembers(members);
        return groupChatRepository.save(group);
    }

    @Transactional
    public void addMember(Long groupId, Long userId) {
        GroupChat group = groupChatRepository.findById(groupId)
            .orElseThrow(() -> new RuntimeException("Group not found"));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        group.getMembers().add(user);
        groupChatRepository.save(group);
    }

    @Transactional
    public void removeMember(Long groupId, Long userId) {
        GroupChat group = groupChatRepository.findById(groupId)
            .orElseThrow(() -> new RuntimeException("Group not found"));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (group.getAdmin().getId().equals(userId)) {
            throw new RuntimeException("Cannot remove admin from group");
        }

        group.getMembers().remove(user);
        groupChatRepository.save(group);
    }

    @Transactional
    public void updateGroup(Long groupId, GroupChatDTO groupDTO) {
        GroupChat group = groupChatRepository.findById(groupId)
            .orElseThrow(() -> new RuntimeException("Group not found"));

        if (groupDTO.getName() != null) {
            group.setName(groupDTO.getName());
        }
        if (groupDTO.getDescription() != null) {
            group.setDescription(groupDTO.getDescription());
        }
        group.setPrivate(groupDTO.isPrivate());

        groupChatRepository.save(group);
    }
} 