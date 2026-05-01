package com.projectmanager.repository;

import com.projectmanager.model.Project;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Project document operations.
 */
@Repository
public interface ProjectRepository extends MongoRepository<Project, String> {

    /** Find all projects where the given userId is listed in the members array */
    @Query("{ 'members.userId': ?0 }")
    List<Project> findByMembersUserId(String userId);

    /** Find all projects owned by a specific user */
    List<Project> findByOwnerId(String ownerId);
}
