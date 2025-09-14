package me.alllexey123.itmoqueue.services

import me.alllexey123.itmoqueue.model.Teacher
import me.alllexey123.itmoqueue.repositories.TeacherRepository
import org.springframework.stereotype.Service

@Service
class TeacherService(private val teacherRepository: TeacherRepository) {

    fun save(teacher: Teacher): Teacher {
        return teacherRepository.save(teacher)
    }

}