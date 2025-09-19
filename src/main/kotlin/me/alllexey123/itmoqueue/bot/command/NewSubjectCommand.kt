package me.alllexey123.itmoqueue.bot.command

import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.bot.state.EnterSubjectNameState
import me.alllexey123.itmoqueue.bot.state.StateManager
import me.alllexey123.itmoqueue.services.Telegram
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.message.Message
import me.alllexey123.itmoqueue.bot.extensions.*

@Component
class NewSubjectCommand(
    private val telegram: Telegram,
    private val stateManager: StateManager,
    private val enterSubjectNameState: EnterSubjectNameState
) : CommandHandler {

    override fun handle(message: Message) {
        val chat = message.chat
        val messageBuilder = SendMessage.builder()
            .chatId(chat.id)
            .replyToMessageId(message.messageId)
            .withForceReply("Введите новое название предмета (отмена - /cancel):")

        stateManager.setHandler(chat.id, enterSubjectNameState)
        telegram.execute(messageBuilder)
    }


    override fun command() = "new_subject"

    override fun scope() = Scope.GROUP

}