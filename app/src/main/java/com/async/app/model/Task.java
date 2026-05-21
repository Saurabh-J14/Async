package com.async.app.model;

public class Task {
    // Status constants
    public static final String STATUS_TODO = "TODO";
    public static final String STATUS_REVIEW = "REVIEW";
    public static final String STATUS_DONE = "DONE";

    private String id;
    private String title;
    private String description;
    private String priority;
    private String category;
    private String dueDate;
    private boolean isCompleted;
    private String status; // "TODO", "REVIEW", "DONE"
    private String assignedToEmail;
    private String workspaceId;
    private String assignedDate;
    private String completedDate;

    public Task(String id, String title, String description, String priority, String category, String dueDate, boolean isCompleted, String status, String assignedToEmail, String workspaceId, String assignedDate, String completedDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.category = category;
        this.dueDate = dueDate;
        this.isCompleted = isCompleted;
        this.status = status;
        this.assignedToEmail = assignedToEmail;
        this.workspaceId = workspaceId;
        this.assignedDate = assignedDate;
        this.completedDate = completedDate;
    }

    public Task(String id, String title, String description, String priority, String category, String dueDate, boolean isCompleted, String status, String assignedToEmail, String workspaceId) {
        this(id, title, description, priority, category, dueDate, isCompleted, status, assignedToEmail, workspaceId, "May 20, 2026", isCompleted ? "May 21, 2026" : "Pending");
    }

    public Task(String id, String title, String description, String priority, String category, String dueDate, boolean isCompleted) {
        this(id, title, description, priority, category, dueDate, isCompleted, isCompleted ? STATUS_DONE : STATUS_TODO, "user@async.com", "1");
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAssignedToEmail() {
        return assignedToEmail;
    }

    public void setAssignedToEmail(String assignedToEmail) {
        this.assignedToEmail = assignedToEmail;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getAssignedDate() {
        return assignedDate;
    }

    public void setAssignedDate(String assignedDate) {
        this.assignedDate = assignedDate;
    }

    public String getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(String completedDate) {
        this.completedDate = completedDate;
    }
}

