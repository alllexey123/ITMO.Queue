package me.alllexey123.itmoqueue.bot

import jakarta.transaction.Transactional
import me.alllexey123.itmoqueue.model.Group
import me.alllexey123.itmoqueue.services.GroupService
import me.alllexey123.itmoqueue.services.UserService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberUpdated

@Component
class MyChatMemberHandler(
    private val groupService: GroupService,
    private val userService: UserService
) {


    @Transactional
    fun handle(update: ChatMemberUpdated) {
        if (update.newChatMember.status == "left") {
            onLeft(update)
        }
        if (update.newChatMember.status == "member") {
            onJoin(update)
        }
    }

    fun onLeft(update: ChatMemberUpdated) {

    }

    fun onJoin(update: ChatMemberUpdated) {
        val group = groupService.findByChatId(update.chat.id)
        if (group == null) {
            onChatFirstJoin(update)
        } else {
            onChatReturn(update, group)
        }
    }

    fun onChatFirstJoin(update: ChatMemberUpdated) {
        val addedById = update.from.id
        val addedByUser = userService.getOrCreateByTelegramId(addedById, update.from.userName)
        val group = groupService.getOrCreateByChatId(update.chat.id)
        group.addedBy = addedByUser
    }

    fun onChatReturn(update: ChatMemberUpdated, group: Group) {
        val addedById = update.from.id
        val addedByUser = userService.getOrCreateByTelegramId(addedById, update.from.userName)
        group.addedBy = addedByUser
    }
}