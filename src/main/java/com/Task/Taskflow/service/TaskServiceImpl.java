package com.Task.Taskflow.service;


import com.Task.Taskflow.dto.TaskRequest;
import com.Task.Taskflow.dto.TaskResponse;
import com.Task.Taskflow.entity.Task;
import com.Task.Taskflow.entity.TaskStatus;
import com.Task.Taskflow.entity.User;
import com.Task.Taskflow.exception.TaskNotFoundException;
import com.Task.Taskflow.repository.TaskRepository;
import com.Task.Taskflow.repository.UserRepository;
import com.Task.Taskflow.security.CurrentUserService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public TaskServiceImpl(
            TaskRepository taskRepository,
            UserRepository userRepository,CurrentUserService currentUserService) {

        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    @CacheEvict(
            value = "userTasks",
            key = "#userId"
    )
    @Override
    public TaskResponse createTask(
            Long userId,
            TaskRequest request) {

        User currentUser = currentUserService.getCurrentUser();

        if (!currentUser.getId().equals(userId)) {
            throw new AccessDeniedException(
                    "You cannot create a task for another user"
            );
        }

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority())
                .dueDate(request.getDueDate())
                .status(TaskStatus.TODO)
                .createdAt(LocalDateTime.now())
                .user(currentUser)
                .build();

        return convertToResponse(
                taskRepository.save(task)
        );
    }

    @Override
    public List<TaskResponse> getAllTasks() {

        return taskRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Cacheable(
            value = "userTasks",
            key = "#userId"
    )
    @Override
    public List<TaskResponse> getTasksByUser(Long userId) {

        User currentUser = currentUserService.getCurrentUser();

        if (!currentUser.getId().equals(userId)) {
            throw new AccessDeniedException(
                    "You cannot access another user's tasks"
            );
        }

        return taskRepository.findByUserId(userId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

//    @Override
//    public TaskResponse getTaskById(Long id) {
//
//        Task task = taskRepository.findById(id)
//                .orElseThrow(() ->
//                        new TaskNotFoundException(
//                                "Task not found with id: " + id
//                        )
//                );
//
//        return convertToResponse(task);
//    }

//    @Override
//    public TaskResponse updateTask(Long id, TaskRequest request) {
//
//        Task existingTask = taskRepository.findById(id)
//                .orElseThrow(() ->
//                        new TaskNotFoundException(
//                                "Task not found with id: " + id
//                        )
//                );
//
//        existingTask.setTitle(request.getTitle());
//        existingTask.setDescription(request.getDescription());
//        existingTask.setPriority(request.getPriority());
//        existingTask.setDueDate(request.getDueDate());
//
//        existingTask.setUpdatedAt(LocalDateTime.now());
//
//        Task updatedTask = taskRepository.save(existingTask);
//
//        return convertToResponse(updatedTask);
//    }

//    @Override
//    public void deleteTask(Long id) {
//
//        Task existingTask = taskRepository.findById(id)
//                .orElseThrow(() ->
//                        new TaskNotFoundException(
//                                "Task not found with id: " + id
//                        )
//                );
//
//        taskRepository.delete(existingTask);
//    }

    private TaskResponse convertToResponse(Task task) {

        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .dueDate(task.getDueDate())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .userId(task.getUser().getId())
                .build();
    }

//    @Override
//    public TaskResponse updateTaskStatus(Long id, TaskStatus status) {
//
//        Task task = taskRepository.findById(id)
//                .orElseThrow(() ->
//                        new TaskNotFoundException(
//                                "Task not found with id: " + id
//                        )
//                );
//
//        task.setStatus(status);
//        task.setUpdatedAt(LocalDateTime.now());
//
//        Task updatedTask = taskRepository.save(task);
//
//        return convertToResponse(updatedTask);
//    }

    private Task getTaskForCurrentUser(Long taskId) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new TaskNotFoundException(
                                "Task not found with id: " + taskId
                        ));

        User currentUser = currentUserService.getCurrentUser();

        if (!task.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(
                    "You cannot access another user's task"
            );
        }

        return task;
    }


    public TaskResponse getTaskById(Long id) {

        Task task = getTaskForCurrentUser(id);

        return convertToResponse(task);
    }

    public TaskResponse updateTask(
            Long id,
            TaskRequest request) {

        Task task = getTaskForCurrentUser(id);

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());
        task.setUpdatedAt(LocalDateTime.now());

        return convertToResponse(
                taskRepository.save(task)
        );
    }

    public void deleteTask(Long id) {

        Task task = getTaskForCurrentUser(id);

        taskRepository.delete(task);
    }

    public TaskResponse updateTaskStatus(
            Long id,
            TaskStatus status) {

        Task task = getTaskForCurrentUser(id);

        task.setStatus(status);
        task.setUpdatedAt(LocalDateTime.now());

        return convertToResponse(
                taskRepository.save(task)
        );
    }
}