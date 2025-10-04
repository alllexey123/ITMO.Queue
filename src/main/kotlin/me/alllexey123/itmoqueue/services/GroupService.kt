package me.alllexey123.itmoqueue.services

import me.alllexey123.itmoqueue.model.Group
import me.alllexey123.itmoqueue.repositories.GroupRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class GroupService(private val groupRepository: GroupRepository) {

    fun findById(groupId: Long): Group? {
        return groupRepository.findByIdOrNull(groupId)
    }

    fun findAll(): List<Group> {
        return groupRepository.findAll()
    }

    fun save(group: Group): Group {
        return groupRepository.save(group)
    }
    
    fun findByChatId(chatId: Long): Group? {
        return groupRepository.findByChatId(chatId)
    }
    
    fun getOrCreateByChatId(chatId: Long): Group {
        val group = findByChatId(chatId)
        if (group == null) {
            return save(Group(
                name = null,
                chatId = chatId
            ))
        }
        return group
    }
}