package me.alllexey123.itmoqueue.services

import jakarta.annotation.PostConstruct
import me.alllexey123.itmoqueue.model.Group
import me.alllexey123.itmoqueue.model.GroupSettings
import me.alllexey123.itmoqueue.repositories.GroupRepository
import me.alllexey123.itmoqueue.repositories.GroupSettingsRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat

@Service
class GroupService(
    private val groupRepository: GroupRepository,
    private val telegram: Telegram,
    private val groupSettingsRepository: GroupSettingsRepository
) {

    @PostConstruct
    fun init() {
        findAll().forEach { g -> {
            if (g.settings == null) {
                val groupSettings = GroupSettings(group = g)
                g.settings = groupSettings
                groupSettingsRepository.save(groupSettings)
                groupRepository.save(g)
            }
        } }
    }

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
        val group = findByChatId(chatId) ?: save(
            Group(
                name = name,
                chatId = chatId
            )
        )
        var needsSave = false
        if (group.name == null) {
            group.name = name
            needsSave = true
        }
        if (group.settings == null) {
            val settings = GroupSettings(group = group)
            groupSettingsRepository.save(settings)
            needsSave = true
        }
        return if (needsSave) save(group) else group
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