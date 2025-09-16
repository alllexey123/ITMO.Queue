package me.alllexey123.itmoqueue.bot.callback

import org.telegram.telegrambots.meta.api.objects.CallbackQuery

interface CallbackHandler {

    fun handle(callbackQuery: CallbackQuery)

    fun prefix(): String

    fun removePrefix(raw: String): String {
        return raw.substring(prefix().length).trim()
    }

    fun addPrefix(data: String): String {
        return prefix() + " " + data
    }
}