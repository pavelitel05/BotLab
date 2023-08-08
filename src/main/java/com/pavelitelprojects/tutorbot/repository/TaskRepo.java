package com.pavelitelprojects.tutorbot.repository;

import com.pavelitelprojects.tutorbot.entity.task.CompleteStatus;
import com.pavelitelprojects.tutorbot.entity.task.Task;
import com.pavelitelprojects.tutorbot.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepo extends JpaRepository<Task, UUID> {
    boolean existsByUsersContainingAndIsInCreation(User user, Boolean isInCreation);
    Task findTaskByUsersContainingAndIsInCreation(User user, Boolean isInCreation);
    void deleteByUsersContainingAndIsInCreation(User user, Boolean isInCreation);
    int countAllByUsersContainingAndIsFinishedAndCompleteStatus(User user,
                                                                Boolean isFinished,
                                                                CompleteStatus completeStatus);
}
