
package com.chat.ui;

public class User {
    public final int id;
    public final String name;
    public final String avatarPath;
    public final boolean isOnline;

    public User(int id, String name, String avatarPath, boolean isOnline) {
        this.id = id;
        this.name = name;
        this.avatarPath = avatarPath;
        this.isOnline = isOnline;
    }
} 