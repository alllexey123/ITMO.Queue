package me.alllexey123.itmoqueue.repositories

import me.alllexey123.itmoqueue.model.QueueEntry
import org.springframework.data.jpa.repository.JpaRepository

interface QueueEntryRepository : JpaRepository<QueueEntry, Long> {
}