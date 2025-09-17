package me.alllexey123.itmoqueue.bot.state

import me.alllexey123.itmoqueue.bot.Scope
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.generics.TelegramClient

abstract class StateHandler {

    val chatData = mutableMapOf<Long, String>()

    // return true if success
    abstract fun handle(message: Message): Boolean

    abstract fun scope(): Scope

    fun groupChatOnlyError(client: TelegramClient, message: Message) {
        client.execute(
            SendMessage.builder()
                .text("Эта возможность доступна только для групп, введите /cancel")
                .replyToMessageId(message.messageId)
                .chatId(message.chatId)
                .build()
        )
    }

    fun privateChatOnlyError(client: TelegramClient, message: Message) {
        client.execute(
            SendMessage.builder()
                .text("Эта возможность доступна только для ЛС, введите /cancel")
                .replyToMessageId(message.messageId)
                .chatId(message.chatId)
                .build()
        )
    }

    fun getChatData(chatId: Long): String? {
        return chatData[chatId]
    }

    fun setChatData(chatId: Long, data: String) {
        chatData[chatId] = data
    }

    fun removeChatData(chatId: Long) {
        chatData.remove(chatId)
    }
}