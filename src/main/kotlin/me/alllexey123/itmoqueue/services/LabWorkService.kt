package me.alllexey123.itmoqueue.services

import me.alllexey123.itmoqueue.model.LabWork
import me.alllexey123.itmoqueue.repositories.LabWorkRepository
import org.springframework.stereotype.Service

@Service
class LabWorkService(private val labWorkRepository: LabWorkRepository) {

    fun save(labWork: LabWork): LabWork {
        return labWorkRepository.save(labWork)
    }
}