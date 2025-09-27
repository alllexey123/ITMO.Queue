package me.alllexey123.itmoqueue.bot

import me.alllexey123.itmoqueue.model.Group
import me.alllexey123.itmoqueue.model.Membership
import me.alllexey123.itmoqueue.model.User
import me.alllexey123.itmoqueue.services.Telegram
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.message.Message

class MessageContext (
    val message: Message,
    val isPrivate: Boolean = message.chat.isUserChat,
    val user: User,
    val membership: Membership?, // null if private
    val group: Group? = membership?.group,
    val chatId: Long = message.chatId,
    val text: String = message.text
) {
    fun send(): SendMessage.SendMessageBuilder<*, *> {
        return SendMessage.builder()
            .chatId(chatId)
            .let { it ->
                if (message.messageThreadId != null && message.isTopicMessage()) {
                    it.messageThreadId(message.messageThreadId)
                }
                it
            }
    }

    fun sendReply(): SendMessage.SendMessageBuilder<*, *> {
        return send().replyToMessageId(message.messageId)
    }

    fun notEnoughPermissions(): SendMessage {
        return sendReply().text("Упс! У вас недостаточно прав для этого действия.").build()
    }

    fun isAdmin(): Boolean {
        return membership?.type == Membership.Type.ADMIN
    }

    fun requireAdmin(telegram: Telegram): Boolean {
        if (isAdmin()) return true
        telegram.execute(notEnoughPermissions())
        return false
    }
}