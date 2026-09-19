package com.Task.Taskflow.service;

import com.Task.Taskflow.dto.TaskRequest;
import com.Task.Taskflow.dto.TaskResponse;
import com.Task.Taskflow.entity.TaskStatus;

import java.util.List;

public interface TaskService {

    TaskResponse createTask(Long userId, TaskRequest request);

    List<TaskResponse> getAllTasks();

    List<TaskResponse> getTasksByUser(Long userId);

    TaskResponse getTaskById(Long id);

    TaskResponse updateTask(Long id, TaskRequest request);

    void deleteTask(Long id);

    TaskResponse updateTaskStatus(Long id, TaskStatus status);
}