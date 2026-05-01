package com.projectmanager.dto.project;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * Request body for adding a member to a project.
 */
@Data
public class AddMemberRequest {

    @NotBlank(message = "User email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Role is required")
    @Pattern(regexp = "ADMIN|MEMBER", message = "Role must be ADMIN or MEMBER")
    private String role;
}
