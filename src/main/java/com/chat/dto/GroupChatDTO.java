package com.chat.dto;

import lombok.Data;
import java.util.Set;

@Data
public class GroupChatDTO {
    private String name;
    private String description;
    private Set<Long> memberIds;
    private boolean isPrivate;
} 