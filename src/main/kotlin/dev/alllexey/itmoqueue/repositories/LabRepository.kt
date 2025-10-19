package dev.alllexey.itmoqueue.repositories

import dev.alllexey.itmoqueue.model.Lab
import org.springframework.data.jpa.repository.JpaRepository

interface LabRepository : JpaRepository<Lab, Long> {

    fun findByShortId(id: String): Lab?
}