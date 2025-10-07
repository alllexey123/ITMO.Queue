package me.alllexey123.itmoqueue.services

import me.alllexey123.itmoqueue.model.Group
import me.alllexey123.itmoqueue.repositories.GroupRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat

@Service
class GroupService(private val groupRepository: GroupRepository, private val telegram: Telegram) {

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

    fun getOrCreateByChatIdAndName(chatId: Long, name: String?): Group {
        val group = findByChatId(chatId)
        if (group == null) {
            return save(
                Group(
                    name = name,
                    chatId = chatId
                )
            )
        } else {
            if (group.name == null) {
                group.name = name
                return save(group)
            }
        }
        return group
    }

    fun refreshGroupNames() {
        findAll().forEach { group ->
            try {
                val getGroupInfo = GetChat.builder().chatId(group.chatId).build()
                val res = telegram.execute(getGroupInfo)
                group.name = res.title
                save(group)
            } catch (e: Exception) {
            }
        }
    }
}