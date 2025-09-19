package me.alllexey123.itmoqueue.bot.state

import me.alllexey123.itmoqueue.bot.Encoder
import me.alllexey123.itmoqueue.bot.Scope
import org.telegram.telegrambots.meta.api.objects.message.Message

abstract class StateHandler : Encoder {

    val chatData = mutableMapOf<Long, List<String>>()

    // return true if success
    abstract fun handle(message: Message): Boolean

    abstract fun scope(): Scope

    fun getChatData(chatId: Long): List<String>? {
        return chatData[chatId]?.map { s -> decodeParam(s) }
    }

    fun setChatData(chatId: Long, vararg data: Any?) {
        chatData[chatId] = data.map { o -> encodeParam(o) }
    }

    fun removeChatData(chatId: Long) {
        chatData.remove(chatId)
    }
}