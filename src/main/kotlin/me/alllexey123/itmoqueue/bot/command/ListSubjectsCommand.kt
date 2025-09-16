package me.alllexey123.itmoqueue.bot.command

import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.services.GroupService
import me.alllexey123.itmoqueue.services.TelegramService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.message.Message

@Component
class ListSubjectsCommand(private val groupService: GroupService, private val telegramService: TelegramService) :
    CommandHandler {

    override fun handle(message: Message) {
        val chat = message.chat
        val group = groupService.getOrCreateByChatId(chat.id)
        val subjects = group.subjects
        val messageBuilder = SendMessage.builder()
            .chatId(message.chat.id)
//            .replyToMessageId(message.messageId)

        if (subjects.isEmpty()) {
            messageBuilder.text(
                """
                Пока что не добавлен ни один предмет
                Добавьте предметы, используя команду /new_subject
            """.trimIndent()
            )
        } else {
            var text = StringBuilder("Список предметов: \n\n")
            var i = 1
            for (subject in subjects) {
                text = text.append("$i. ${subject.name}").append("\n")
                i++
            }

            messageBuilder.text(text.toString())
        }

        telegramService.client.execute(messageBuilder.build())
    }

    override fun command() = "list_subjects"

    override fun scope() = Scope.GROUP
}