package me.alllexey123.itmoqueue.bot.command

import me.alllexey123.itmoqueue.bot.Scope
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.generics.TelegramClient
import me.alllexey123.itmoqueue.bot.extensions.*

interface CommandHandler {

    fun handle(message: Message)

    fun command(): String

    fun scope(): Scope

    fun groupChatOnlyError(client: TelegramClient, message: Message) {
        client.execute(
            SendMessage.builder()
                .text("Эта команда доступна только для групп")
                .withReplyTo(message)
        )
    }

    fun userChatOnlyError(client: TelegramClient, message: Message) {
        client.execute(
            SendMessage.builder()
                .text("Эта команда доступна только для ЛС")
                .withReplyTo(message)
        )
    }
}