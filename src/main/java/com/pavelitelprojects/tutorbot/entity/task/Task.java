package com.pavelitelprojects.tutorbot.entity.task;

import com.pavelitelprojects.tutorbot.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tasks")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Task {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(name = "tittle")
    String tittle;

    @Column(name = "text_content")
    String textContent;

    @Column(name = "actual_message_id")
    Integer messageId;

    @Column(name = "in_creation")
    Boolean isInCreation;

    @ManyToMany
    @JoinTable(
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"),
            name = "tasks_teacher_student"
    )
    List<User> users;

}
