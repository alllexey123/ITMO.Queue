package me.alllexey123.itmoqueue.services

import me.alllexey123.itmoqueue.model.LabWork
import me.alllexey123.itmoqueue.repositories.LabWorkRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class LabWorkService(private val labWorkRepository: LabWorkRepository) {

    fun save(labWork: LabWork): LabWork {
        return labWorkRepository.save(labWork)
    }

    fun findById(id: Long): LabWork? {
        return labWorkRepository.findByIdOrNull(id)
    }

    fun deleteById(id: Long) {
        labWorkRepository.deleteById(id)
    }

    fun flush() {
        labWorkRepository.flush()
    }
}