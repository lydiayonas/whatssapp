package com.chat.controller;

import com.chat.service.WhatsAppBotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/whatsapp")
public class WhatsAppController {

    private final WhatsAppBotService whatsAppBotService;

    @Autowired
    public WhatsAppController(WhatsAppBotService whatsAppBotService) {
        this.whatsAppBotService = whatsAppBotService;
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestBody String message) {
        try {
            whatsAppBotService.sendMessage(message);
            return ResponseEntity.ok("Message sent successfully");
        } catch (Exception e) {
            log.error("Error sending message", e);
            return ResponseEntity.internalServerError().body("Failed to send message: " + e.getMessage());
        }
    }
} 