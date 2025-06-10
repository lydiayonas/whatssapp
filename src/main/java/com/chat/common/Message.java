package com.chat.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message {
    private String sender;
    private String receiver;
    private String content;
    private String timestamp;  // Changed to String for better serialization
    private MessageType type;
    private byte[] fileContent;
    private String fileName;
    private String fileType;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public enum MessageType {
        CHAT,
        LOGIN,
        REGISTER,
        LOGOUT,
        ERROR,
        FILE
    }

    public Message() {
        this.timestamp = LocalDateTime.now().format(formatter);
    }

    public Message(String sender, String receiver, String content, MessageType type) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.type = type;
        this.timestamp = LocalDateTime.now().format(formatter);
    }

    // Getters and Setters
    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return LocalDateTime.parse(timestamp, formatter);
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp.format(formatter);
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public byte[] getFileContent() {
        return fileContent;
    }

    public void setFileContent(byte[] fileContent) {
        this.fileContent = fileContent;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s -> %s: %s", 
            timestamp, sender, receiver, content);
    }
} 