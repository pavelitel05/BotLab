package com.pavelitelprojects.tutorbot.entity.timetable;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

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
}
