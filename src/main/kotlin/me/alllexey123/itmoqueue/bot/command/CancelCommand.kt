package me.alllexey123.itmoqueue.bot.command

import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.bot.state.StateManager
import me.alllexey123.itmoqueue.services.TelegramService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.message.Message

@Component
class CancelCommand(private val stateManager: StateManager,
                    private val telegramService: TelegramService
) : CommandHandler {

    override fun handle(message: Message) {
        val handler = stateManager.removeHandler(message.chatId)
        val sendMessage = SendMessage.builder()
            .chatId(message.chatId)
            .text(if (handler == null) "Никакое действие не требовалось" else "Действие отменено")
            .replyToMessageId(message.messageId)
            .build()
        telegramService.client.execute(sendMessage)
    }

    override fun command() = "cancel"

    override fun scope() = Scope.ANY
}