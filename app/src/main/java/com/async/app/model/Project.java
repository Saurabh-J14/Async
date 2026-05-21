package com.async.app.model;

public class Project {
    private String id;
    private String name;
    private String description;
    private String category;
    private int progress;
    private int membersCount;

    public Project(String id, String name, String description, String category, int progress, int membersCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.progress = progress;
        this.membersCount = membersCount;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getMembersCount() {
        return membersCount;
    }

    public void setMembersCount(int membersCount) {
        this.membersCount = membersCount;
    }
}
