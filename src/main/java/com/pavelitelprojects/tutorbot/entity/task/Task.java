package com.pavelitelprojects.tutorbot.entity.task;

import com.pavelitelprojects.tutorbot.entity.user.Role;
import com.pavelitelprojects.tutorbot.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.NoSuchElementException;
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

    @Column(name = "text_content")
    String textContent;

    @Column(name = "actual_message_id")
    Integer messageId;

    @Column(name = "actual_menu_id")
    Integer menuId;

    @Enumerated(EnumType.STRING)
    CompleteStatus completeStatus;

    @Column(name = "in_creation")
    Boolean isInCreation;

    @Column(name = "has_media")
    Boolean hasMedia;

    @Column(name = "is_finished")
    Boolean isFinished;

    @ManyToMany
    @JoinTable(
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"),
            name = "tasks_teacher_student"
    )
    List<User> users;

    public User getStudent() {
        for (User user : users) {
            if (Role.STUDENT.equals(user.getRole())) {
                return user;
            }
        }
        throw new NoSuchElementException("No student for task " + id);
    }

    public User getTeacher() {
        for (User user : users) {
            if (Role.TEACHER.equals(user.getRole())) {
                return user;
            }
        }
        throw new NoSuchElementException("No teacher for task " + id);
    }


    public void changeUser(User student) {
        if (Role.TEACHER.equals(student.getRole())) {
            throw new IllegalArgumentException("Asked student, teacher given");
        }
        users.removeIf(user -> Role.STUDENT.equals(user.getRole()));
        users.add(student);
    }

}
