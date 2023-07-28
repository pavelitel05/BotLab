package com.pavelitelprojects.tutorbot.entity.timetable;

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
@Table(name = "timetable")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TimeTable {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(name = "tittle")
    String tittle;

    @Column(name = "description")
    String description;

    @Enumerated(EnumType.STRING)
    WeekDay weekDay;

    @Column(name = "hour")
    Short hour;

    @Column(name = "minute")
    Short minute;

    @ManyToMany
    @JoinTable(
            joinColumns = @JoinColumn(name = "timetable_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"),
            name = "users_timetable"
    )
    List<User> users;
}
