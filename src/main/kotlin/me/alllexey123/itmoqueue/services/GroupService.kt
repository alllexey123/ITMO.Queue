package me.alllexey123.itmoqueue.services

import me.alllexey123.itmoqueue.model.Group
import me.alllexey123.itmoqueue.model.GroupSettings
import me.alllexey123.itmoqueue.repositories.GroupRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat

@Service
class GroupService(
    private val groupRepository: GroupRepository,
    private val telegram: Telegram
) {

    fun findById(groupId: Long?): Group? {
        if (groupId == null) return null
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
        val existingGroup = findByChatId(chatId)
        if (existingGroup != null) {
            var needsSave = false
            if (existingGroup.name != name) {
                existingGroup.name = name
                needsSave = true
            }
            return if (needsSave) save(existingGroup) else existingGroup
        }

        val newGroup = Group(
            name = name,
            chatId = chatId,
        )

        val newSettings = GroupSettings(group = newGroup)

        newGroup.settings = newSettings

        return save(newGroup)
    }

    fun refreshGroupNames() {
        findAll().forEach { group ->
            try {
                val getGroupInfo = GetChat.builder().chatId(group.chatId).build()
                val res = telegram.execute(getGroupInfo)
                group.name = res.title
                save(group)
            } catch (_: Exception) {
            }
        }
    }
}