package me.alllexey123.itmoqueue.repositories

import me.alllexey123.itmoqueue.model.Lab
import me.alllexey123.itmoqueue.model.QueueEntry
import me.alllexey123.itmoqueue.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface QueueEntryRepository : JpaRepository<QueueEntry, Long> {

    fun findByLabAndUserAndDone(lab: Lab, user: User, done: Boolean): QueueEntry?
}