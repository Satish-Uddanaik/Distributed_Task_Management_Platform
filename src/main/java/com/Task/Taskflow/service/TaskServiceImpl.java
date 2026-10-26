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

import com.Task.Taskflow.dto.TaskCreatedEvent;
import com.Task.Taskflow.kafka.KafkaProducerService;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final KafkaProducerService kafkaProducerService;

    public TaskServiceImpl(
            TaskRepository taskRepository,
            UserRepository userRepository,
            CurrentUserService currentUserService,
            KafkaProducerService kafkaProducerService) {

        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.kafkaProducerService = kafkaProducerService;
    }


    // =========================================================
    // CREATE TASK
    // =========================================================

    @Override
    @CacheEvict(value = "userTasks", allEntries = true)
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
                .status(TaskStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .user(currentUser)
                .build();

        Task savedTask = taskRepository.save(task);

        TaskCreatedEvent event = new TaskCreatedEvent(
                "TASK_CREATED",
                savedTask.getId(),
                savedTask.getTitle(),
                savedTask.getUser().getId(),
                savedTask.getCreatedAt()
        );

        kafkaProducerService.publishTaskCreated(event);

        return convertToResponse(savedTask);
    }


    // =========================================================
    // GET ALL TASKS
    // =========================================================

    @Override
    public List<TaskResponse> getAllTasks() {

        return taskRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }


    // =========================================================
    // GET USER TASKS
    // =========================================================

    @Override
    @Cacheable(
            value = "userTasks",
            key = "#userId"
    )
    public List<TaskResponse> getTasksByUser(Long userId) {

        System.out.println("🔥 DATABASE METHOD EXECUTED");

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


    // =========================================================
    // GET TASK BY ID
    // =========================================================

    @Override
    public TaskResponse getTaskById(Long id) {

        Task task = getTaskForCurrentUser(id);

        return convertToResponse(task);
    }


    // =========================================================
    // UPDATE TASK
    // =========================================================

    @Override
    @CacheEvict(
            value = "userTasks",
            allEntries = true
    )
    public TaskResponse updateTask(
            Long id,
            TaskRequest request) {

        Task task = getTaskForCurrentUser(id);

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());

        task.setUpdatedAt(LocalDateTime.now());

        Task updatedTask = taskRepository.save(task);

        return convertToResponse(updatedTask);
    }


    // =========================================================
    // DELETE TASK
    // =========================================================

    @Override
    @CacheEvict(
            value = "userTasks",
            allEntries = true
    )
    public void deleteTask(Long id) {

        Task task = getTaskForCurrentUser(id);

        taskRepository.delete(task);
    }


    // =========================================================
    // UPDATE STATUS
    // =========================================================

    @Override
    @CacheEvict(
            value = "userTasks",
            allEntries = true
    )
    public TaskResponse updateTaskStatus(
            Long id,
            TaskStatus status) {

        Task task = getTaskForCurrentUser(id);

        task.setStatus(status);
        task.setUpdatedAt(LocalDateTime.now());

        Task updatedTask = taskRepository.save(task);

        return convertToResponse(updatedTask);
    }


    // =========================================================
    // GET TASK + VERIFY CURRENT USER
    // =========================================================

    private Task getTaskForCurrentUser(Long taskId) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new TaskNotFoundException(
                                "Task not found with id: " + taskId
                        )
                );

        User currentUser = currentUserService.getCurrentUser();

        if (!task.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(
                    "You cannot access another user's task"
            );
        }

        return task;
    }


    // =========================================================
    // ENTITY → RESPONSE DTO
    // =========================================================

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
}