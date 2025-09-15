package me.alllexey123.itmoqueue.bot.command

import org.telegram.telegrambots.meta.api.objects.message.Message

interface CommandHandler {

    fun handle(message: Message)

    fun command(): String
}