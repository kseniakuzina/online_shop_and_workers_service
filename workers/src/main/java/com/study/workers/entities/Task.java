package com.study.workers.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "t_task")
public class Task {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="user_id")
    private User user;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    private TaskType taskType;

    @Enumerated(EnumType.STRING)
    private TaskPurpose taskPurpose;

    private Long orderId;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        orderId = orderId;
    }

    public TaskPurpose getTaskPurpose() {
        return taskPurpose;
    }

    public void setTaskPurpose(TaskPurpose taskPurpose) {
        this.taskPurpose = taskPurpose;
    }

    public Task(User user, String name, String description, TaskType taskType, TaskPurpose taskPurpose, Long orderId) {
        this.user = user;
        this.name = name;
        this.description = description;
        this.taskType = taskType;
        this.taskPurpose = taskPurpose;
        this.orderId = orderId;

    }
    public Task() {
    }

}
