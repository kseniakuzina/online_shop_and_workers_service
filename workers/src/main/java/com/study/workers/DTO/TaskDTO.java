package com.study.workers.DTO;


public class TaskDTO {
    private String username;
    private String name;
    private String description;

    public String getUsername() {
        return username;
    }

    public void setUsername(String user_id) {
        this.username = username;
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

    public TaskDTO(String username, String name, String description) {
        this.username = username;
        this.name = name;
        this.description = description;
    }

}
