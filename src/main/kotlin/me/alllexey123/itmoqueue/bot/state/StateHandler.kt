package me.alllexey123.itmoqueue.bot.state

import me.alllexey123.itmoqueue.bot.MessageContext
import me.alllexey123.itmoqueue.bot.Scope

abstract class StateHandler {

    val chatData = mutableMapOf<Long, List<String>>()

    // return true if success
    abstract fun handle(context: MessageContext): Boolean

    abstract fun scope(): Scope

    fun getChatData(chatId: Long): List<String>? {
        return chatData[chatId]
    }

    fun setChatData(chatId: Long, vararg data: Any?) {
        chatData[chatId] = data.map { o -> o.toString() }
    }

    fun removeChatData(chatId: Long) {
        chatData.remove(chatId)
    }
}