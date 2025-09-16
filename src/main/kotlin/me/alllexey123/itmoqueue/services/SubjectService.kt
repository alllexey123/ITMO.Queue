package me.alllexey123.itmoqueue.services

import me.alllexey123.itmoqueue.model.Subject
import me.alllexey123.itmoqueue.repositories.SubjectRepository
import org.springframework.stereotype.Service

@Service
class SubjectService(private val subjectRepository: SubjectRepository) {

    fun save(subject: Subject): Subject {
        return subjectRepository.save(subject)
    }
}