package me.alllexey123.itmoqueue.services

import me.alllexey123.itmoqueue.model.Subject
import me.alllexey123.itmoqueue.repositories.SubjectRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class SubjectService(private val subjectRepository: SubjectRepository) {

    fun save(subject: Subject): Subject {
        return subjectRepository.save(subject)
    }

    fun findById(id: Long): Subject? {
        return subjectRepository.findByIdOrNull(id)
    }

    fun deleteById(id: Long) {
        subjectRepository.deleteById(id)
    }

    fun flush() {
        subjectRepository.flush()
    }
}