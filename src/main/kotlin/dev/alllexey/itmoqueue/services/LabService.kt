package dev.alllexey.itmoqueue.services

import dev.alllexey.itmoqueue.ItmoQueueProperties
import dev.alllexey.itmoqueue.model.Group
import dev.alllexey.itmoqueue.model.Lab
import dev.alllexey.itmoqueue.model.Subject
import dev.alllexey.itmoqueue.repositories.LabRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class LabService(private val labRepository: LabRepository, private val itmoQueueProperties: ItmoQueueProperties) {

    fun save(lab: Lab): Lab {
        return labRepository.save(lab)
    }

    fun create(group: Group, name: String, subject: Subject): Lab {
        val lab =  save(Lab(
            name = name,
            group = group,
            subject = subject,
            shortId = generateUniqueShortId()
        ))
        return lab
    }

    private fun generateUniqueShortId(): String {
        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        val idLength = 8
        var shortId: String
        do {
            shortId = (1..idLength)
                .map { alphabet.random() }
                .joinToString("")
        } while (labRepository.findByShortId(shortId) != null)
        return shortId
    }

    fun findByShortId(id: String): Lab? {
        return labRepository.findByShortId(id)
    }

    fun getLabUrl(lab: Lab): String {
        return "${itmoQueueProperties.origin}/lab/${lab.shortId}"
    }

    fun findById(id: Long?): Lab? {
        if (id == null) return null
        return labRepository.findByIdOrNull(id)
    }

    fun deleteById(id: Long) {
        labRepository.deleteById(id)
        flush()
    }

    fun flush() {
        labRepository.flush()
    }
}