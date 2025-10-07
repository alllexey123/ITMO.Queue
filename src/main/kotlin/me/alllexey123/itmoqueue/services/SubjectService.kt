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
            group = group,
            shortId = generateUniqueShortId()
        ))
        return subject
    }

    private fun generateUniqueShortId(): String {
        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        val idLength = 8
        var shortId: String
        do {
            shortId = (1..idLength)
                .map { alphabet.random() }
                .joinToString("")
        } while (subjectRepository.findByShortId(shortId) != null)
        return shortId
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