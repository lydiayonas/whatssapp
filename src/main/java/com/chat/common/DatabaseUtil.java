package com.chat.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DatabaseUtil {
    private static final String DB_URL = "jdbc:h2:file:./chat";
    private static final String USER = "sa";
    private static final String PASS = "";

    private static final DateTimeFormatter DB_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    public static void initialize() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Create users table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                         "username VARCHAR(255) PRIMARY KEY NOT NULL," +
                         "password VARCHAR(255) NOT NULL," +
                         "online BOOLEAN DEFAULT FALSE," +
                         "last_seen VARCHAR(255)"
                         +")");

            // Create messages table
            stmt.execute("CREATE TABLE IF NOT EXISTS messages (" +
                         "id INT AUTO_INCREMENT PRIMARY KEY," +
                         "sender VARCHAR(255) NOT NULL," +
                         "receiver VARCHAR(255) NOT NULL," +
                         "content VARCHAR(MAX) NOT NULL," +
                         "timestamp VARCHAR(255) NOT NULL," +
                         "type VARCHAR(255) NOT NULL"
                         +")");

            // Add test users if they don't exist
            addTestUser("muni", "123");
            addTestUser("msgana", "456");

            System.out.println("Database initialized successfully.");

        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }

    private static void addTestUser(String username, String password) {
        String sql = "INSERT INTO users (username, password, online, last_seen) " +
                    "SELECT ?, ?, false, ? " +
                    "WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, LocalDateTime.now().format(DB_TIMESTAMP_FORMATTER));
            pstmt.setString(4, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding test user: " + e.getMessage());
        }
    }

    public static boolean userExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking if user exists: " + e.getMessage());
        }
        return false;
    }

    public static boolean authenticateUser(String username, String password) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ? AND password = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error authenticating user: " + e.getMessage());
        }
        return false;
    }

    public static boolean registerUser(User user) {
        String sql = "INSERT INTO users (username, password, online, last_seen) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setBoolean(3, user.isOnline());
            pstmt.setString(4, user.getLastSeen());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (e.getErrorCode() == 23505) { // H2 error code for unique constraint violation
                System.err.println("Registration failed: Username already exists.");
            } else {
                System.err.println("Error registering user: " + e.getMessage());
            }
        }
        return false;
    }

    public static boolean saveMessage(Message message) {
        String sql = "INSERT INTO messages (sender, receiver, content, timestamp, type) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, message.getSender());
            pstmt.setString(2, message.getReceiver());
            pstmt.setString(3, message.getContent());
            pstmt.setString(4, message.getTimestamp().format(DB_TIMESTAMP_FORMATTER)); // Store formatted timestamp
            pstmt.setString(5, message.getType().name());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error saving message: " + e.getMessage());
        }
        return false;
    }

    public static void updateLastSeen(String username) {
        String sql = "UPDATE users SET last_seen = ?, online = ? WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, LocalDateTime.now().format(DB_TIMESTAMP_FORMATTER));
            pstmt.setBoolean(2, false); // Set user offline when updating last seen
            pstmt.setString(3, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating last seen: " + e.getMessage());
        }
    }
} 