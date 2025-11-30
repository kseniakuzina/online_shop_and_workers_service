package com.ksu.workers.repository;

import com.ksu.workers.entities.Task;
import com.ksu.workers.entities.TaskPurpose;
import com.ksu.workers.entities.TaskType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task,Long> {
    List<Task> findByUserId(Long userId);
    Task findByTaskPurposeAndOrderId(TaskPurpose taskPurpose, Long orderId);
    List<Task> findByTaskPurposeAndTaskType(TaskPurpose taskPurpose, TaskType taskType);
}
