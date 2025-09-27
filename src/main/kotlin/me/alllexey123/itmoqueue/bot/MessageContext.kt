package me.alllexey123.itmoqueue.bot

import me.alllexey123.itmoqueue.model.Group
import me.alllexey123.itmoqueue.model.Membership
import me.alllexey123.itmoqueue.model.User
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
            .messageThreadId(message.messageThreadId)
    }

    fun sendReply(): SendMessage.SendMessageBuilder<*, *> {
        return send().replyToMessageId(message.messageId)
    }
}