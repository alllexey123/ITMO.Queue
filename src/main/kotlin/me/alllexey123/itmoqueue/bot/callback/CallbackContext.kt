package me.alllexey123.itmoqueue.bot.callback

import me.alllexey123.itmoqueue.bot.extensions.edit
import me.alllexey123.itmoqueue.model.Group
import me.alllexey123.itmoqueue.model.ManagedMessage
import me.alllexey123.itmoqueue.model.Membership
import me.alllexey123.itmoqueue.model.User
import me.alllexey123.itmoqueue.services.ManagedMessageService
import me.alllexey123.itmoqueue.services.Telegram
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.chat.Chat
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage
import org.telegram.telegrambots.meta.api.objects.message.Message

class CallbackContext(
    val query: CallbackQuery,
    val membership: Membership?,
    val data: CallbackData,
    private val telegram: Telegram,
    private val managedMessageService: ManagedMessageService,

    val message: MaybeInaccessibleMessage = query.message,
    val isPrivate: Boolean = message.isUserMessage,
    val chat: Chat = message.chat,
    val user: User,
    val group: Group?,
    val id: String = query.id,
    val chatId: Long = chat.id,
    val messageId: Int = message.messageId,
    val managedMessage: ManagedMessage
) {
    fun send(threadId: Int? = null): SendMessage.SendMessageBuilder<*, *> {
        return SendMessage.builder()
            .chatId(chatId)
            .let { it ->
                if (isTopicMessage()) {
                    message as Message
                    it.messageThreadId(threadId ?: message.messageThreadId)
                }
                it
            }
    }

    fun edit(): EditMessageText.EditMessageTextBuilder<*, *> {
        return managedMessage.edit()
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

    fun answer(text: String) {
        telegram.execute(answerBuilder().text(text).build())
    }

    fun isAdmin(): Boolean {
        return membership?.type == Membership.Type.ADMIN
    }

    fun requireAdmin(): Boolean {
        if (isAdmin()) return true
        telegram.execute(notEnoughPermissions())
        return false
    }

    fun requireAdmin(membership: Membership?): Boolean {
        if (membership == null) return false
        if (membership.type == Membership.Type.ADMIN) return true
        telegram.execute(notEnoughPermissions())
        return false
    }

    fun deleteMessage() {
        telegram.execute(deleteBuilder().build())
        managedMessageService.unregister(chatId, messageId)
    }

    fun isTopicMessage(): Boolean {
        message as Message
        return message.messageThreadId != null && message.isTopicMessage()
    }
}