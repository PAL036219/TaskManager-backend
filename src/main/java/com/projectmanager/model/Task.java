package com.projectmanager.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a task within a project.
 *
 * Priority: LOW | MEDIUM | HIGH | URGENT
 * Status:   TODO | IN_PROGRESS | REVIEW | DONE
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tasks")
public class Task {

    @Id
    private String id;

    /** The project this task belongs to */
    private String projectId;

    private String title;

    private String description;

    /** Due date for the task */
    private LocalDate dueDate;

    /** Priority: LOW, MEDIUM, HIGH, URGENT */
    private String priority;

    /** Status: TODO, IN_PROGRESS, REVIEW, DONE */
    private String status;

    /** ID of the User this task is assigned to */
    private String assigneeId;

    /** Denormalized assignee name for display */
    private String assigneeName;

    /** ID of the User who created this task */
    private String createdById;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "TODO";
        }
        if (this.priority == null) {
            this.priority = "MEDIUM";
        }
    }

    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
