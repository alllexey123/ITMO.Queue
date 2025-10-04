package me.alllexey123.itmoqueue.repositories

import me.alllexey123.itmoqueue.model.Lab
import org.springframework.data.jpa.repository.JpaRepository

interface LabRepository : JpaRepository<Lab, Long> {

}