package com.chat.client;

import com.chat.common.Message;
import com.google.gson.Gson;
import javafx.application.Platform;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ChatClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5000;
    
    private final Socket socket;
    private final PrintWriter out;
    private final BufferedReader in;
    private final ExecutorService executor;
    private final Gson gson;
    private Consumer<Message> messageHandler;
    private String username;

    public ChatClient() throws IOException {
        socket = new Socket(SERVER_HOST, SERVER_PORT);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        executor = Executors.newSingleThreadExecutor();
        gson = new Gson();
        startMessageListener();
    }

    private void startMessageListener() {
        executor.execute(() -> {
            try {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    Message message = gson.fromJson(inputLine, Message.class);
                    if (messageHandler != null) {
                        Platform.runLater(() -> messageHandler.accept(message));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void setMessageHandler(Consumer<Message> handler) {
        this.messageHandler = handler;
    }

    public void login(String username, String password) {
        this.username = username;
        Message loginMessage = new Message(username, "SERVER", password, Message.MessageType.LOGIN);
        sendMessage(loginMessage);
    }

    public void register(String username, String password) {
        Message registerMessage = new Message(username, "SERVER", password, Message.MessageType.REGISTER);
        sendMessage(registerMessage);
    }

    public void sendChatMessage(String receiver, String content) {
        Message chatMessage = new Message(username, receiver, content, Message.MessageType.CHAT);
        sendMessage(chatMessage);
    }

    public void logout() {
        Message logoutMessage = new Message(username, "SERVER", "", Message.MessageType.LOGOUT);
        sendMessage(logoutMessage);
        close();
    }

    private void sendMessage(Message message) {
        out.println(gson.toJson(message));
    }

    public void close() {
        try {
            executor.shutdown();
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUsername() {
        return username;
    }
} 