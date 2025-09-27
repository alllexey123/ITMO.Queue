package me.alllexey123.itmoqueue.services

import me.alllexey123.itmoqueue.model.Group
import me.alllexey123.itmoqueue.model.Subject
import me.alllexey123.itmoqueue.repositories.SubjectRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class SubjectService(private val subjectRepository: SubjectRepository) {

    fun save(subject: Subject): Subject {
        return subjectRepository.save(subject)
    }

    fun create(group: Group, name: String): Subject {
        val subject = save(Subject(
            name = name,
            group = group
        ))
        return subject
    }

    fun findById(id: Long?): Subject? {
        if (id == null) return null
        return subjectRepository.findByIdOrNull(id)
    }

    fun deleteById(id: Long) {
        subjectRepository.deleteById(id)
        flush()
    }

    fun flush() {
        subjectRepository.flush()
    }
}