package com.pavelitelprojects.tutorbot.repository;

import com.pavelitelprojects.tutorbot.entity.timetable.TimeTable;
import com.pavelitelprojects.tutorbot.entity.timetable.WeekDay;
import com.pavelitelprojects.tutorbot.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TimeTableRepo extends JpaRepository<TimeTable, UUID> {
    List<TimeTable> findAllByUsersContainingAndWeekDayAndInCreation(User user, WeekDay weekDay, Boolean isInCreation);
    TimeTable findTimeTableById(UUID id);
}
