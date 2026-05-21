package com.async.app.network.model;

import com.google.gson.annotations.SerializedName;

public class AssignedUser {
    @SerializedName("name")
    private String name;

    @SerializedName("role")
    private String role;

    @SerializedName("username")
    private String username;

    public AssignedUser(String name, String role, String username) {
        this.name = name;
        this.role = role;
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
