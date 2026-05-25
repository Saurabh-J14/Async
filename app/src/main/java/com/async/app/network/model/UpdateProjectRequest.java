package com.async.app.network.model;

import com.google.gson.annotations.SerializedName;

public class UpdateProjectRequest {
    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("department")
    private String department;

    public UpdateProjectRequest(String name, String description, String department) {
        this.name = name;
        this.description = description;
        this.department = department;
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

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
