package me.alllexey123.itmoqueue.services

import me.alllexey123.itmoqueue.model.User
import me.alllexey123.itmoqueue.repositories.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class UserService (private val userRepository: UserRepository) {

    fun findById(userId: Long): User? {
        return userRepository.findByIdOrNull(userId)
    }

}