package com.projectmanager.controller;

import com.projectmanager.dto.task.TaskResponse;
import com.projectmanager.repository.UserRepository;
import com.projectmanager.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for dashboard analytics.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private UserRepository userRepository;

    private String getCurrentUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    /**
     * GET /api/dashboard/stats
     * Returns task summary statistics for the current user.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(dashboardService.getStats(userId));
    }

    /**
     * GET /api/dashboard/overdue
     * Returns overdue tasks assigned to the current user.
     */
    @GetMapping("/overdue")
    public ResponseEntity<List<TaskResponse>> getOverdueTasks(
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(dashboardService.getOverdueTasks(userId));
    }

    /**
     * GET /api/dashboard/recent
     * Returns the 10 most recently updated tasks assigned to the current user.
     */
    @GetMapping("/recent")
    public ResponseEntity<List<TaskResponse>> getRecentTasks(
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(dashboardService.getRecentTasks(userId));
    }
}
