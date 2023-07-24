package com.pavelitelprojects.tutorbot.repository;

import com.pavelitelprojects.tutorbot.entity.task.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TaskRepo extends JpaRepository<Task, UUID> {
}
