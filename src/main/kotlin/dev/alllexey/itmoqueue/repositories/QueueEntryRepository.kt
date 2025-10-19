package dev.alllexey.itmoqueue.repositories

import dev.alllexey.itmoqueue.model.Lab
import dev.alllexey.itmoqueue.model.QueueEntry
import dev.alllexey.itmoqueue.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface QueueEntryRepository : JpaRepository<QueueEntry, Long> {

    fun findByLabAndUserAndDone(lab: Lab, user: User, done: Boolean): QueueEntry?
}