package me.alllexey123.itmoqueue.repositories

import me.alllexey123.itmoqueue.model.Queue
import me.alllexey123.itmoqueue.model.QueueEntry
import me.alllexey123.itmoqueue.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface QueueEntryRepository : JpaRepository<QueueEntry, Long> {

    fun findByQueueAndUserAndDone(queue: Queue, user: User, done: Boolean): QueueEntry?
}