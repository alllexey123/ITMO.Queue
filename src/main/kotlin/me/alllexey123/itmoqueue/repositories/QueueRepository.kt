package me.alllexey123.itmoqueue.repositories

import me.alllexey123.itmoqueue.model.Queue
import org.springframework.data.jpa.repository.JpaRepository

interface QueueRepository : JpaRepository<Queue, Long> {
}