package com.projectmanager.dto.project;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for creating or updating a project.
 */
@Data
public class ProjectRequest {

    @NotBlank(message = "Project name is required")
    private String name;

    private String description;
}
