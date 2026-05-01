package com.projectmanager.controller;

import com.projectmanager.dto.project.AddMemberRequest;
import com.projectmanager.dto.project.ProjectRequest;
import com.projectmanager.dto.project.ProjectResponse;
import com.projectmanager.repository.UserRepository;
import com.projectmanager.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for project management.
 * All endpoints require authentication (enforced by SecurityConfig).
 */
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserRepository userRepository;

    // ── Helper to resolve current user's MongoDB ID from Spring Security principal ──
    private String getCurrentUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in DB"))
                .getId();
    }

    /**
     * GET /api/projects
     * List all projects for the currently authenticated user.
     */
    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getMyProjects(
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(projectService.getProjectsForUser(userId));
    }

    /**
     * POST /api/projects
     * Create a new project. The creator becomes the project Admin.
     */
    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody ProjectRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = getCurrentUserId(userDetails);
        ProjectResponse response = projectService.createProject(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/projects/{projectId}
     * Get a specific project (must be a member).
     */
    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> getProject(
            @PathVariable String projectId,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(projectService.getProjectById(projectId, userId));
    }

    /**
     * PUT /api/projects/{projectId}
     * Update a project (Admin only).
     */
    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable String projectId,
            @Valid @RequestBody ProjectRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(projectService.updateProject(projectId, request, userId));
    }

    /**
     * DELETE /api/projects/{projectId}
     * Delete a project and all its tasks (Admin only).
     */
    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable String projectId,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = getCurrentUserId(userDetails);
        projectService.deleteProject(projectId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/projects/{projectId}/members
     * Add a user to the project by email (Admin only).
     */
    @PostMapping("/{projectId}/members")
    public ResponseEntity<ProjectResponse> addMember(
            @PathVariable String projectId,
            @Valid @RequestBody AddMemberRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(projectService.addMember(projectId, request, userId));
    }

    /**
     * DELETE /api/projects/{projectId}/members/{userId}
     * Remove a member from the project (Admin only).
     */
    @DeleteMapping("/{projectId}/members/{memberId}")
    public ResponseEntity<ProjectResponse> removeMember(
            @PathVariable String projectId,
            @PathVariable String memberId,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(projectService.removeMember(projectId, memberId, userId));
    }
}
