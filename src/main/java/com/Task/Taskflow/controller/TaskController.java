package com.Task.Taskflow.controller;

import com.Task.Taskflow.dto.TaskRequest;
import com.Task.Taskflow.dto.TaskResponse;
import com.Task.Taskflow.dto.TaskStatusRequest;
import com.Task.Taskflow.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }


    // =========================================================
    // CREATE
    // POST /api/tasks/user/{userId}
    // =========================================================

    @PostMapping("/user/{userId}")
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable Long userId,
            @Valid @RequestBody TaskRequest request) {

        TaskResponse response =
                taskService.createTask(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    // =========================================================
    // GET ALL
    // GET /api/tasks
    // =========================================================

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAllTasks() {

        return ResponseEntity.ok(
                taskService.getAllTasks()
        );
    }


    // =========================================================
    // GET USER TASKS
    // GET /api/tasks/user/{userId}
    // =========================================================

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TaskResponse>> getTasksByUser(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                taskService.getTasksByUser(userId)
        );
    }


    // =========================================================
    // GET ONE
    // GET /api/tasks/{id}
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                taskService.getTaskById(id)
        );
    }


    // =========================================================
    // UPDATE
    // PUT /api/tasks/{id}
    // =========================================================

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request) {

        return ResponseEntity.ok(
                taskService.updateTask(id, request)
        );
    }


    // =========================================================
    // DELETE
    // DELETE /api/tasks/{id}
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id) {

        taskService.deleteTask(id);

        return ResponseEntity.noContent().build();
    }


    // =========================================================
    // UPDATE STATUS
    // PATCH /api/tasks/{id}/status
    // =========================================================

    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @PathVariable Long id,
            @Valid @RequestBody TaskStatusRequest request) {

        return ResponseEntity.ok(
                taskService.updateTaskStatus(
                        id,
                        request.getStatus()
                )
        );
    }
}