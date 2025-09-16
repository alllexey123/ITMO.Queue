package me.alllexey123.itmoqueue.bot.command

import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.services.GroupService
import me.alllexey123.itmoqueue.services.TelegramService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.message.Message

@Component
class NewLabCommand(private val telegramService: TelegramService, private val groupService: GroupService) :
    CommandHandler {

    override fun handle(message: Message) {
        val chat = message.chat
        val group = groupService.getOrCreateByChatId(chat.id)
        val sendMessageBuilder = SendMessage.builder()
            .chatId(chat.id)
            .replyToMessageId(message.messageId)
        val subjects = group.subjects
        if (subjects.isNotEmpty()) {

        } else {

        }
        TODO()
    }

    override fun command() = "new_lab"

    override fun scope() = Scope.GROUP
}