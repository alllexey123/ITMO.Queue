package me.alllexey123.itmoqueue.bot.command

import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.bot.state.AddSubjectState
import me.alllexey123.itmoqueue.bot.state.StateManager
import me.alllexey123.itmoqueue.services.TelegramService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard

@Component
class NewSubjectCommand(
    private val telegramService: TelegramService,
    private val stateManager: StateManager,
    private val addSubjectState: AddSubjectState
) : CommandHandler {

    override fun handle(message: Message) {
        val chat = message.chat
        val messageBuilder = SendMessage.builder()
            .chatId(chat.id)
            .text("Введите название предмета (/cancel для отмены):")
            .replyToMessageId(message.messageId)
            .replyMarkup(
                ForceReplyKeyboard.builder()
                    .inputFieldPlaceholder("предмет")
                    .forceReply(true)
                    .selective(true)
                    .build()
            ).build()

        stateManager.setHandler(chat.id, addSubjectState)
        telegramService.client.execute(messageBuilder)
    }

    override fun command() = "new_subject"

    override fun scope() = Scope.GROUP

}