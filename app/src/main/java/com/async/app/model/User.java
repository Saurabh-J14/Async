package com.async.app.model;

public class User {
    private String id;
    private String fullName;
    private String email;
    private String password;
    private String role;
    private String profileImage;
    private String username;
    private String department;

    public User(String fullName, String email, String password, String role) {
        this.id = "";
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.profileImage = null;
        this.username = "";
        this.department = "";
    }

    public User(String fullName, String email, String password) {
        this(fullName, email, password, "EMPLOYEE");
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
