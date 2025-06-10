package com.chat.repository;

import com.chat.model.GroupChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupChatRepository extends JpaRepository<GroupChat, Long> {
    @Query("SELECT g FROM GroupChat g JOIN g.members m WHERE m.id = :userId")
    List<GroupChat> findByMembersId(@Param("userId") Long userId);
} 