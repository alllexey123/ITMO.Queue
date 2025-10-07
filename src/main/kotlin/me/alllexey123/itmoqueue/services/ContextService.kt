package me.alllexey123.itmoqueue.services

import me.alllexey123.itmoqueue.model.Group
import me.alllexey123.itmoqueue.model.Membership
import me.alllexey123.itmoqueue.model.User
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage
import org.telegram.telegrambots.meta.api.objects.message.Message

@Service
class ContextService(
    private val userService: UserService,
    private val groupService: GroupService,
    private val membershipService: MembershipService
) {

    fun getUser(telegramId: Long, telegramName: String?): User {
        return userService.getOrCreateByTelegramId(telegramId, telegramName = telegramName)
    }

    fun getUser(message: Message): User {
        return getUser(message.from.id, message.from.userName)
    }

    fun getUser(user: org.telegram.telegrambots.meta.api.objects.User): User {
        return getUser(user.id, user.userName)
    }


    fun getGroup(chatId: Long, name: String?): Group {
        return groupService.getOrCreateByChatIdAndName(chatId, name)
    }

    fun getGroup(message: Message): Group {
        return getGroup(message.chatId, message.chat.title)
    }

    fun getGroup(message: MaybeInaccessibleMessage): Group {
        return getGroup(message.chatId, message.chat.title)
    }

    fun getMembership(chatId: Long, chatName: String, telegramId: Long, telegramName: String?): Membership {

        val found = membershipService.findByChatIdAndUserId(chatId, telegramId)
        return found ?: membershipService.save(
            Membership(
                group = getGroup(chatId, chatName),
                user = getUser(telegramId, telegramName),
                type = Membership.Type.MEMBER
            )
        )
    }

    fun getMembership(message: Message): Membership? {
        if (message.isUserMessage) return null
        return getMembership(message.chatId, message.chat.title, message.from.id, message.from.userName)
    }

    fun getMembership(callbackQuery: CallbackQuery): Membership? {
        if (callbackQuery.message.isUserMessage) return null
        return getMembership(
            callbackQuery.message.chatId,
            callbackQuery.message.chat.title,
            callbackQuery.from.id,
            callbackQuery.from.userName
        )
    }
}