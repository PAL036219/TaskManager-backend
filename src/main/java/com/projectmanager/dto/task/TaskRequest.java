package com.projectmanager.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

/**
 * Request body for creating or updating a task.
 */
@Data
public class TaskRequest {

    @NotBlank(message = "Task title is required")
    private String title;

    private String description;

    private LocalDate dueDate;

    @Pattern(regexp = "LOW|MEDIUM|HIGH|URGENT", message = "Priority must be LOW, MEDIUM, HIGH, or URGENT")
    private String priority;

    @Pattern(regexp = "TODO|IN_PROGRESS|REVIEW|DONE", message = "Status must be TODO, IN_PROGRESS, REVIEW, or DONE")
    private String status;

    /** ID of the user to assign this task to */
    private String assigneeId;
}
