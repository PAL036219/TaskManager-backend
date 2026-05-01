package com.projectmanager.service;

import com.projectmanager.dto.task.TaskRequest;
import com.projectmanager.dto.task.TaskResponse;
import com.projectmanager.exception.ResourceNotFoundException;
import com.projectmanager.exception.UnauthorizedException;
import com.projectmanager.model.Project;
import com.projectmanager.model.Task;
import com.projectmanager.model.User;
import com.projectmanager.repository.TaskRepository;
import com.projectmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic for task management.
 *
 * RBAC rules enforced here:
 * - Admin: full CRUD on all tasks in the project
 * - Member: create tasks (self-assign), update/view own tasks, update status of own tasks
 */
@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectService projectService;

    // ────────────────────────────────── CRUD ──────────────────────────────────────

    /**
     * Get all tasks for a project with optional filters.
     * User must be a project member.
     */
    public List<TaskResponse> getTasks(String projectId, String userId,
                                       String status, String priority, String assigneeId) {
        Project project = projectService.findProjectOrThrow(projectId);
        assertMember(project, userId);

        List<Task> tasks;

        // Apply filters dynamically
        if (StringUtils.hasText(status) && StringUtils.hasText(priority) && StringUtils.hasText(assigneeId)) {
            tasks = taskRepository.findByProjectId(projectId).stream()
                    .filter(t -> t.getStatus().equals(status)
                            && t.getPriority().equals(priority)
                            && t.getAssigneeId() != null && t.getAssigneeId().equals(assigneeId))
                    .collect(Collectors.toList());
        } else if (StringUtils.hasText(status) && StringUtils.hasText(priority)) {
            tasks = taskRepository.findByProjectIdAndStatusAndPriority(projectId, status, priority);
        } else if (StringUtils.hasText(status) && StringUtils.hasText(assigneeId)) {
            tasks = taskRepository.findByProjectId(projectId).stream()
                    .filter(t -> t.getStatus().equals(status)
                            && t.getAssigneeId() != null && t.getAssigneeId().equals(assigneeId))
                    .collect(Collectors.toList());
        } else if (StringUtils.hasText(status)) {
            tasks = taskRepository.findByProjectIdAndStatus(projectId, status);
        } else if (StringUtils.hasText(priority)) {
            tasks = taskRepository.findByProjectIdAndPriority(projectId, priority);
        } else if (StringUtils.hasText(assigneeId)) {
            tasks = taskRepository.findByProjectIdAndAssigneeId(projectId, assigneeId);
        } else {
            tasks = taskRepository.findByProjectId(projectId);
        }

        return tasks.stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Get a single task by ID. User must be a project member.
     */
    public TaskResponse getTaskById(String projectId, String taskId, String userId) {
        Project project = projectService.findProjectOrThrow(projectId);
        assertMember(project, userId);
        Task task = findTaskOrThrow(taskId, projectId);
        return toResponse(task);
    }

    /**
     * Create a new task in the project.
     * - Admin: can assign to any project member
     * - Member: can only self-assign
     */
    public TaskResponse createTask(String projectId, TaskRequest request, String userId) {
        Project project = projectService.findProjectOrThrow(projectId);
        assertMember(project, userId);

        boolean isAdmin = projectService.isAdminInProject(project, userId);

        // Determine assignee
        String assigneeId = request.getAssigneeId();
        if (!isAdmin) {
            // Members can only self-assign
            assigneeId = userId;
        }

        // Validate assignee is a project member
        String finalAssigneeId = assigneeId;
        if (StringUtils.hasText(finalAssigneeId)) {
            boolean assigneeIsMember = project.getMembers().stream()
                    .anyMatch(m -> m.getUserId().equals(finalAssigneeId));
            if (!assigneeIsMember) {
                throw new UnauthorizedException("Assignee is not a project member");
            }
        }

        // Denormalize assignee name
        String assigneeName = null;
        if (StringUtils.hasText(finalAssigneeId)) {
            assigneeName = project.getMembers().stream()
                    .filter(m -> m.getUserId().equals(finalAssigneeId))
                    .map(m -> m.getUserName())
                    .findFirst().orElse(null);
        }

        Task task = Task.builder()
                .projectId(projectId)
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .priority(StringUtils.hasText(request.getPriority()) ? request.getPriority() : "MEDIUM")
                .status(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "TODO")
                .assigneeId(finalAssigneeId)
                .assigneeName(assigneeName)
                .createdById(userId)
                .build();
        task.onCreate();

        Task saved = taskRepository.save(task);
        return toResponse(saved);
    }

    /**
     * Update an existing task.
     * - Admin: can update all fields on any task
     * - Member: can update their own task's status, description; cannot reassign
     */
    public TaskResponse updateTask(String projectId, String taskId, TaskRequest request, String userId) {
        Project project = projectService.findProjectOrThrow(projectId);
        assertMember(project, userId);

        Task task = findTaskOrThrow(taskId, projectId);
        boolean isAdmin = projectService.isAdminInProject(project, userId);
        boolean isOwner = userId.equals(task.getCreatedById()) || userId.equals(task.getAssigneeId());

        if (!isAdmin && !isOwner) {
            throw new UnauthorizedException("You can only update tasks assigned to you or created by you");
        }

        // Update fields
        if (StringUtils.hasText(request.getTitle())) {
            if (isAdmin) {
                task.setTitle(request.getTitle());
            }
        }

        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }

        if (request.getDueDate() != null && isAdmin) {
            task.setDueDate(request.getDueDate());
        }

        if (StringUtils.hasText(request.getPriority()) && isAdmin) {
            task.setPriority(request.getPriority());
        }

        if (StringUtils.hasText(request.getStatus())) {
            task.setStatus(request.getStatus());
        }

        // Only admins can reassign tasks
        if (StringUtils.hasText(request.getAssigneeId()) && isAdmin) {
            String newAssigneeId = request.getAssigneeId();
            boolean memberExists = project.getMembers().stream()
                    .anyMatch(m -> m.getUserId().equals(newAssigneeId));
            if (!memberExists) {
                throw new UnauthorizedException("New assignee is not a project member");
            }
            task.setAssigneeId(newAssigneeId);
            // Update denormalized name
            String newAssigneeName = project.getMembers().stream()
                    .filter(m -> m.getUserId().equals(newAssigneeId))
                    .map(m -> m.getUserName())
                    .findFirst().orElse(null);
            task.setAssigneeName(newAssigneeName);
        }

        task.onUpdate();
        Task saved = taskRepository.save(task);
        return toResponse(saved);
    }

    /**
     * Delete a task. Only project admins can delete tasks.
     */
    public void deleteTask(String projectId, String taskId, String userId) {
        Project project = projectService.findProjectOrThrow(projectId);
        assertAdmin(project, userId);
        Task task = findTaskOrThrow(taskId, projectId);
        taskRepository.delete(task);
    }

    // ───────────────────────────────── Helpers ──────────────────────────────────

    private Task findTaskOrThrow(String taskId, String projectId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));
        if (!task.getProjectId().equals(projectId)) {
            throw new ResourceNotFoundException("Task does not belong to this project");
        }
        return task;
    }

    private void assertMember(Project project, String userId) {
        if (!projectService.isMemberOfProject(project, userId)) {
            throw new UnauthorizedException("You are not a member of this project");
        }
    }

    private void assertAdmin(Project project, String userId) {
        if (!projectService.isAdminInProject(project, userId)) {
            throw new UnauthorizedException("Only project admins can perform this action");
        }
    }

    public TaskResponse toResponse(Task task) {
        boolean overdue = task.getDueDate() != null
                && task.getDueDate().isBefore(LocalDate.now())
                && !"DONE".equals(task.getStatus());

        return TaskResponse.builder()
                .id(task.getId())
                .projectId(task.getProjectId())
                .title(task.getTitle())
                .description(task.getDescription())
                .dueDate(task.getDueDate())
                .priority(task.getPriority())
                .status(task.getStatus())
                .assigneeId(task.getAssigneeId())
                .assigneeName(task.getAssigneeName())
                .createdById(task.getCreatedById())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .overdue(overdue)
                .build();
    }
}
