package me.alllexey123.itmoqueue.repositories

import me.alllexey123.itmoqueue.model.Queue
import me.alllexey123.itmoqueue.model.QueueEntry
import me.alllexey123.itmoqueue.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface QueueEntryRepository : JpaRepository<QueueEntry, Long> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM QueueEntry qe WHERE qe.user = :user AND qe.queue = :queue")
    fun deleteByUserAndQueue(user: User, queue: Queue)

    fun findByQueueAndUserAndDone(queue: Queue, user: User, done: Boolean): QueueEntry?
}