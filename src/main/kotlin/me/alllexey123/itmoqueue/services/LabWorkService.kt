package me.alllexey123.itmoqueue.services

import me.alllexey123.itmoqueue.model.Group
import me.alllexey123.itmoqueue.model.LabWork
import me.alllexey123.itmoqueue.model.Subject
import me.alllexey123.itmoqueue.repositories.LabWorkRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class LabWorkService(private val labWorkRepository: LabWorkRepository, private val queueService: QueueService) {

    fun save(labWork: LabWork): LabWork {
        return labWorkRepository.save(labWork)
    }

    fun create(group: Group, name: String, subject: Subject): LabWork {
        val lab =  save(LabWork(
            name = name,
            group = group,
            subject = subject
        ))
        queueService.createDefaultQueue(lab)
        return lab
    }

    fun findById(id: Long?): LabWork? {
        if (id == null) return null
        return labWorkRepository.findByIdOrNull(id)
    }

    fun deleteById(id: Long) {
        labWorkRepository.deleteById(id)
        flush()
    }

    fun flush() {
        labWorkRepository.flush()
    }
}