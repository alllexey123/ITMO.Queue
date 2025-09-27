package me.alllexey123.itmoqueue.bot.callback

import me.alllexey123.itmoqueue.model.Group
import me.alllexey123.itmoqueue.model.Membership
import me.alllexey123.itmoqueue.model.User
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.chat.Chat
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage

class CallbackContext(
    val query: CallbackQuery,
    val membership: Membership?,
    val data: List<String>,

    val message: MaybeInaccessibleMessage = query.message,
    val isPrivate: Boolean = message.isUserMessage,
    val chat: Chat = message.chat,
    val user: User,
    val group: Group?,
    val id: String = query.id,
    val chatId: Long = chat.id,
    val messageId: Int = message.messageId,
) {

    fun asString(index: Int): String {
        return data[index]
    }

    fun asInt(index: Int): Int {
        return data[index].toInt()
    }

    fun asLong(index: Int): Long {
        return data[index].toLong()
    }
}