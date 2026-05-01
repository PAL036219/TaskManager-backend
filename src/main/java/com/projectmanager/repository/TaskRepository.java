package com.projectmanager.repository;

import com.projectmanager.model.Task;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for Task document operations.
 */
@Repository
public interface TaskRepository extends MongoRepository<Task, String> {

    /** Get all tasks for a project */
    List<Task> findByProjectId(String projectId);

    /** Get tasks filtered by project and status */
    List<Task> findByProjectIdAndStatus(String projectId, String status);

    /** Get tasks filtered by project and priority */
    List<Task> findByProjectIdAndPriority(String projectId, String priority);

    /** Get tasks filtered by project and assignee */
    List<Task> findByProjectIdAndAssigneeId(String projectId, String assigneeId);

    /** Get tasks filtered by project, status, and priority */
    List<Task> findByProjectIdAndStatusAndPriority(String projectId, String status, String priority);

    /** Get tasks assigned to a specific user across all projects */
    List<Task> findByAssigneeId(String assigneeId);

    /** Get tasks assigned to a user with specific status */
    List<Task> findByAssigneeIdAndStatus(String assigneeId, String status);

    /** Get overdue tasks for a user (due before today, not DONE) */
    List<Task> findByAssigneeIdAndDueDateBeforeAndStatusNot(String assigneeId, LocalDate date, String status);

    /** Get tasks in multiple projects assigned to a user */
    List<Task> findByProjectIdInAndAssigneeId(List<String> projectIds, String assigneeId);

    /** Get overdue tasks across multiple projects */
    List<Task> findByProjectIdInAndDueDateBeforeAndStatusNot(List<String> projectIds, LocalDate date, String status);

    /** Count tasks by project and status */
    long countByProjectIdAndStatus(String projectId, String status);
}
