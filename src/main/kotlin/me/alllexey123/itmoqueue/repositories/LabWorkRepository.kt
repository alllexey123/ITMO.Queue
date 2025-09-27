package me.alllexey123.itmoqueue.repositories

import me.alllexey123.itmoqueue.model.LabWork
import org.springframework.data.jpa.repository.JpaRepository

interface LabWorkRepository : JpaRepository<LabWork, Long> {

}