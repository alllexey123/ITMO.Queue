package me.alllexey123.itmoqueue.bot.callback

import me.alllexey123.itmoqueue.model.Group
import me.alllexey123.itmoqueue.model.Membership
import me.alllexey123.itmoqueue.model.User
import me.alllexey123.itmoqueue.services.Telegram
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.chat.Chat
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage
import org.telegram.telegrambots.meta.api.objects.message.Message

class CallbackContext(
    val query: CallbackQuery,
    val membership: Membership?,
    val data: CallbackData,

    val message: MaybeInaccessibleMessage = query.message,
    val isPrivate: Boolean = message.isUserMessage,
    val chat: Chat = message.chat,
    val user: User,
    val group: Group?,
    val id: String = query.id,
    val chatId: Long = chat.id,
    val messageId: Int = message.messageId
) : ICallbackData by `data` {
    fun send(): SendMessage.SendMessageBuilder<*, *> {
        return SendMessage.builder()
            .chatId(chatId)
            .let { it ->
                message as Message
                if (message.messageThreadId != null && message.isTopicMessage()) {
                    it.messageThreadId(message.messageThreadId)
                }
                it
            }
    }

    fun notEnoughPermissions(): AnswerCallbackQuery {
        return answerBuilder().text("Упс! У вас недостаточно прав для этого действия.").build()
    }

    fun deleteBuilder(): DeleteMessage.DeleteMessageBuilder<*, *> {
        return DeleteMessage.builder().messageId(messageId).chatId(chatId)
    }

    fun answerBuilder(): AnswerCallbackQuery.AnswerCallbackQueryBuilder<*, *> {
        return AnswerCallbackQuery.builder().callbackQueryId(id)
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