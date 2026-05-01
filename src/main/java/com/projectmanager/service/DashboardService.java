package com.projectmanager.service;

import com.projectmanager.dto.task.TaskResponse;
import com.projectmanager.model.Project;
import com.projectmanager.model.Task;
import com.projectmanager.repository.ProjectRepository;
import com.projectmanager.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Provides aggregated data for the dashboard.
 */
@Service
public class DashboardService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskService taskService;

    /**
     * Get task summary statistics for the current user across all their projects.
     *
     * Returns: total, todo, inProgress, review, done, overdue counts
     */
    public Map<String, Object> getStats(String userId) {
        // Get all project IDs the user is a member of
        List<Project> userProjects = projectRepository.findByMembersUserId(userId);
        List<String> projectIds = userProjects.stream()
                .map(Project::getId)
                .collect(Collectors.toList());

        // Get tasks assigned to the user across those projects
        List<Task> myTasks = taskRepository.findByProjectIdInAndAssigneeId(projectIds, userId);

        long total = myTasks.size();
        long todo = myTasks.stream().filter(t -> "TODO".equals(t.getStatus())).count();
        long inProgress = myTasks.stream().filter(t -> "IN_PROGRESS".equals(t.getStatus())).count();
        long review = myTasks.stream().filter(t -> "REVIEW".equals(t.getStatus())).count();
        long done = myTasks.stream().filter(t -> "DONE".equals(t.getStatus())).count();
        long overdue = myTasks.stream()
                .filter(t -> t.getDueDate() != null
                        && t.getDueDate().isBefore(LocalDate.now())
                        && !"DONE".equals(t.getStatus()))
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("todo", todo);
        stats.put("inProgress", inProgress);
        stats.put("review", review);
        stats.put("done", done);
        stats.put("overdue", overdue);
        stats.put("projectCount", userProjects.size());

        // Tasks by status for chart
        Map<String, Long> byStatus = new HashMap<>();
        byStatus.put("TODO", todo);
        byStatus.put("IN_PROGRESS", inProgress);
        byStatus.put("REVIEW", review);
        byStatus.put("DONE", done);
        stats.put("tasksByStatus", byStatus);

        return stats;
    }

    /**
     * Get overdue tasks assigned to the current user.
     */
    public List<TaskResponse> getOverdueTasks(String userId) {
        List<Task> overdueTasks = taskRepository
                .findByAssigneeIdAndDueDateBeforeAndStatusNot(userId, LocalDate.now(), "DONE");

        return overdueTasks.stream()
                .map(taskService::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get recent tasks assigned to the current user (last 10).
     */
    public List<TaskResponse> getRecentTasks(String userId) {
        List<Task> tasks = taskRepository.findByAssigneeId(userId);

        // Sort by updatedAt descending, take top 10
        return tasks.stream()
                .sorted((a, b) -> {
                    if (a.getUpdatedAt() == null) return 1;
                    if (b.getUpdatedAt() == null) return -1;
                    return b.getUpdatedAt().compareTo(a.getUpdatedAt());
                })
                .limit(10)
                .map(taskService::toResponse)
                .collect(Collectors.toList());
    }
}
