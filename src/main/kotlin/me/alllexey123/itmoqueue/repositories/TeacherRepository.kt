package me.alllexey123.itmoqueue.repositories

import me.alllexey123.itmoqueue.model.Teacher
import org.springframework.data.jpa.repository.JpaRepository

interface TeacherRepository : JpaRepository<Teacher, Long> {
}