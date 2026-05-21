package com.async.app.model;

import java.util.UUID;

public class AppNotification {
    private String id;
    private String title;
    private String message;
    private boolean isRead;
    private String targetUserEmail;
    private long timestamp;

    public AppNotification(String title, String message, String targetUserEmail) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.message = message;
        this.isRead = false;
        this.targetUserEmail = targetUserEmail;
        this.timestamp = System.currentTimeMillis();
    }

    public AppNotification(String id, String title, String message, boolean isRead, String targetUserEmail, long timestamp) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.isRead = isRead;
        this.targetUserEmail = targetUserEmail;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getTargetUserEmail() {
        return targetUserEmail;
    }

    public void setTargetUserEmail(String targetUserEmail) {
        this.targetUserEmail = targetUserEmail;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
