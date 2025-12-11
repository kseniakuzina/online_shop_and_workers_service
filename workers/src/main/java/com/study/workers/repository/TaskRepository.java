package com.study.workers.repository;

import com.study.workers.entities.Task;
import com.study.workers.entities.TaskPurpose;
import com.study.workers.entities.TaskType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task,Long> {
    List<Task> findByUserId(Long userId);
    Task findByTaskPurposeAndOrderId(TaskPurpose taskPurpose, Long orderId);
    List<Task> findByTaskPurposeAndTaskType(TaskPurpose taskPurpose, TaskType taskType);
}
