package dev.alllexey.itmoqueue.services

import dev.alllexey.itmoqueue.model.User
import dev.alllexey.itmoqueue.repositories.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class UserService (private val userRepository: UserRepository) {

    fun findById(userId: Long?): User? {
        if (userId == null) return null
        return userRepository.findByIdOrNull(userId)
    }

    fun findAll(): List<User> {
        return userRepository.findAll()
    }

    fun findByTelegramId(telegramId: Long): User? {
        return userRepository.findByTelegramId(telegramId)
    }

    fun save(user: User): User {
        return userRepository.save(user)
    }

    fun getOrCreateByTelegramId(telegramId: Long, telegramName: String?): User {
        val user = findByTelegramId(telegramId)
        if (user == null) {
            return save(User(
                nickname = telegramName,
                telegramId = telegramId
            ))
        }
        return user
    }
}