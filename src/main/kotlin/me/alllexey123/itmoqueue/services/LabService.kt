package me.alllexey123.itmoqueue.services

import me.alllexey123.itmoqueue.model.Group
import me.alllexey123.itmoqueue.model.Lab
import me.alllexey123.itmoqueue.model.Subject
import me.alllexey123.itmoqueue.model.enums.QueueType
import me.alllexey123.itmoqueue.repositories.LabRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class LabService(private val labRepository: LabRepository, private val queueService: QueueService) {

    fun save(lab: Lab): Lab {
        return labRepository.save(lab)
    }

    fun create(group: Group, name: String, subject: Subject): Lab {
        val lab =  save(Lab(
            name = name,
            group = group,
            subject = subject,
            queueType = QueueType.FIRST_PRIORITY
        ))
        return lab
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