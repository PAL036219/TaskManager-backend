package com.projectmanager.controller;

import com.projectmanager.dto.task.TaskRequest;
import com.projectmanager.dto.task.TaskResponse;
import com.projectmanager.repository.UserRepository;
import com.projectmanager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for task management within a project.
 * All endpoints require authentication.
 */
@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserRepository userRepository;

    private String getCurrentUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    /**
     * GET /api/projects/{projectId}/tasks
     * List tasks with optional filters: status, priority, assigneeId
     */
    @GetMapping
    public ResponseEntity<List<TaskResponse>> getTasks(
            @PathVariable String projectId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String assigneeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = getCurrentUserId(userDetails);
        List<TaskResponse> tasks = taskService.getTasks(projectId, userId, status, priority, assigneeId);
        return ResponseEntity.ok(tasks);
    }

    /**
     * GET /api/projects/{projectId}/tasks/{taskId}
     * Get a single task.
     */
    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getTask(
            @PathVariable String projectId,
            @PathVariable String taskId,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(taskService.getTaskById(projectId, taskId, userId));
    }

    /**
     * POST /api/projects/{projectId}/tasks
     * Create a new task.
     */
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable String projectId,
            @Valid @RequestBody TaskRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = getCurrentUserId(userDetails);
        TaskResponse response = taskService.createTask(projectId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /api/projects/{projectId}/tasks/{taskId}
     * Update a task.
     */
    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable String projectId,
            @PathVariable String taskId,
            @Valid @RequestBody TaskRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(taskService.updateTask(projectId, taskId, request, userId));
    }

    /**
     * DELETE /api/projects/{projectId}/tasks/{taskId}
     * Delete a task (Admin only).
     */
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable String projectId,
            @PathVariable String taskId,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = getCurrentUserId(userDetails);
        taskService.deleteTask(projectId, taskId, userId);
        return ResponseEntity.noContent().build();
    }
}
