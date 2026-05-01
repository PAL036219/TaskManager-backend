package com.projectmanager.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response body for successful authentication (signup or login).
 * Contains the JWT token and basic user info.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String id;
    private String name;
    private String email;
    private String role;
}
