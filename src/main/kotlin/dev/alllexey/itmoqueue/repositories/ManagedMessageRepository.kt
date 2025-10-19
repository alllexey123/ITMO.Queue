package dev.alllexey.itmoqueue.repositories

import dev.alllexey.itmoqueue.model.ManagedMessage
import dev.alllexey.itmoqueue.model.ManagedMessageId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional

interface ManagedMessageRepository : JpaRepository<ManagedMessage, ManagedMessageId> {

    @Transactional
    @Modifying
    @Query("UPDATE ManagedMessage m SET m.updatedAt = CURRENT_TIMESTAMP WHERE m.id = :id")
    fun touch(id: ManagedMessageId)
}