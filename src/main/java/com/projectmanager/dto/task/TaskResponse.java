package com.projectmanager.dto.task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response body for a task.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

    private String id;
    private String projectId;
    private String title;
    private String description;
    private LocalDate dueDate;
    private String priority;
    private String status;
    private String assigneeId;
    private String assigneeName;
    private String createdById;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** Whether the task is overdue (past due date and not DONE) */
    private boolean overdue;
}
