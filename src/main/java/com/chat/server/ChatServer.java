package com.chat.server;

import com.chat.common.DatabaseUtil;
import com.chat.common.Message;
import com.chat.model.User;
import com.chat.model.Chat;
import com.chat.service.AutoResponseService;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class ChatServer {
    private static final int PORT = 5000;
    private final ServerSocket serverSocket;
    private final ExecutorService pool;
    private final ConcurrentHashMap<String, ClientHandler> clients;
    private final Gson gson;
    private final AutoResponseService autoResponseService;

    @Autowired
    public ChatServer(AutoResponseService autoResponseService) throws IOException {
        this.serverSocket = new ServerSocket(PORT);
        this.pool = Executors.newCachedThreadPool();
        this.clients = new ConcurrentHashMap<>();
        this.gson = new Gson();
        this.autoResponseService = autoResponseService;
        DatabaseUtil.initialize();
    }

    public void start() {
        System.out.println("Server started on port " + PORT);
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                pool.execute(clientHandler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    Message message = gson.fromJson(inputLine, Message.class);
                    handleMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                cleanup();
            }
        }

        private void handleMessage(Message message) {
            switch (message.getType()) {
                case LOGIN:
                    handleLogin(message);
                    break;
                case REGISTER:
                    handleRegister(message);
                    break;
                case CHAT:
                    handleChat(message);
                    break;
                case LOGOUT:
                    handleLogout();
                    break;
            }
        }

        private void handleLogin(Message message) {
            if (DatabaseUtil.authenticateUser(message.getSender(), message.getContent())) {
                username = message.getSender();
                clients.put(username, this);
                sendMessage(new Message("SERVER", username, "Login successful", Message.MessageType.CHAT));
            } else {
                sendMessage(new Message("SERVER", message.getSender(), "Login failed", Message.MessageType.ERROR));
            }
        }

        private void handleRegister(Message message) {
            if (DatabaseUtil.registerUser(new com.chat.common.User(message.getSender(), message.getContent()))) {
                sendMessage(new Message("SERVER", message.getSender(), "Registration successful", Message.MessageType.CHAT));
            } else {
                sendMessage(new Message("SERVER", message.getSender(), "Registration failed", Message.MessageType.ERROR));
            }
        }

        private void handleChat(Message message) {
            DatabaseUtil.saveMessage(message);
            ClientHandler receiver = clients.get(message.getReceiver());
            if (receiver != null) {
                receiver.sendMessage(message);
            }
            
            // Generate and send automatic response
            User botUser = new User();
            botUser.setId(1L);
            botUser.setUsername("Bot");
            botUser.setAvatar("bot_avatar.png");
            botUser.setOnline(true);
            
            Chat chat = new Chat();
            chat.setId(1L);
            chat.setName("Auto Response Chat");
            
            Message autoResponse = autoResponseService.generateResponse(message, botUser, chat);
            
            // Send the auto-response back to the sender
            ClientHandler sender = clients.get(message.getSender());
            if (sender != null) {
                sender.sendMessage(autoResponse);
            }
        }

        private void handleLogout() {
            if (username != null) {
                clients.remove(username);
                DatabaseUtil.updateLastSeen(username);
            }
        }

        private void sendMessage(Message message) {
            out.println(gson.toJson(message));
        }

        private void cleanup() {
            try {
                if (username != null) {
                    clients.remove(username);
                    DatabaseUtil.updateLastSeen(username);
                }
                if (out != null) out.close();
                if (in != null) in.close();
                if (clientSocket != null) clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        try {
            ChatServer server = new ChatServer();
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 