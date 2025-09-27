package me.alllexey123.itmoqueue.bot.command

import me.alllexey123.itmoqueue.bot.MessageContext
import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.bot.extensions.withReplyTo
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.generics.TelegramClient

interface CommandHandler {

    fun handle(context: MessageContext)

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