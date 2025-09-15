package me.alllexey123.itmoqueue.repositories

import me.alllexey123.itmoqueue.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {

    fun findByTelegramId(telegramId: Long): User?
}