package dev.alllexey.itmoqueue.repositories

import dev.alllexey.itmoqueue.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {

    fun findByTelegramId(telegramId: Long): User?
}