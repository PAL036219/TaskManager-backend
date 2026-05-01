package com.projectmanager.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Represents a registered user in the system.
 * Password is stored as a BCrypt hash.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id 
    private String id;

    private String name;

    @Indexed(unique = true)
    private String email;

    /** BCrypt-hashed password */
    private String password;

    /** Global role: ADMIN or MEMBER */
    private String role;

    private LocalDateTime createdAt;

    /** Set creation timestamp before persisting */
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.role == null) {
            this.role = "MEMBER";
        }
    }
}
