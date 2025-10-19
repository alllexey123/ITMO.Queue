package dev.alllexey.itmoqueue.repositories

import dev.alllexey.itmoqueue.model.Group
import org.springframework.data.jpa.repository.JpaRepository

interface GroupRepository : JpaRepository<Group, Long> {

    fun findByChatId(chatId: Long): Group?

}