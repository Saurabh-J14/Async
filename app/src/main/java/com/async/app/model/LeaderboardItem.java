package com.async.app.model;

public class LeaderboardItem {
    public final String name;
    public final String email;
    public final String department;
    public final String avatar;
    public final int score;

    public LeaderboardItem(String name, String email, String department, String avatar, int score) {
        this.name = name;
        this.email = email;
        this.department = department;
        this.avatar = avatar;
        this.score = score;
    }
}
