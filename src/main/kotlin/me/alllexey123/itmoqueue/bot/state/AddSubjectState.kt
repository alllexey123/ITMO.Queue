package me.alllexey123.itmoqueue.bot.state

import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.model.Subject
import me.alllexey123.itmoqueue.services.GroupService
import me.alllexey123.itmoqueue.services.SubjectService
import me.alllexey123.itmoqueue.services.TelegramService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard

@Component
class AddSubjectState(
    private val groupService: GroupService,
    private val telegramService: TelegramService,
    private val subjectService: SubjectService
) : StateHandler {

    override fun handle(message: Message): Boolean {
        val chat = message.chat
        val subjectName = message.text.trim()
        val group = groupService.getOrCreateByChatId(chat.id)
        val sendMessage = SendMessage.builder()
            .chatId(chat.id)
            .replyToMessageId(message.messageId)

        if (subjectName.length > 30) {
            sendMessage.text("Название предмета не может иметь длину более 30 символов, попробуйте снова")
                .replyMarkup(
                    ForceReplyKeyboard.builder()
                        .forceReply(true)
                        .selective(true)
                        .build()
                )
            telegramService.client.execute(sendMessage.build())
            return false
        }

        for (subject in group.subjects) {
            if (subject.name.lowercase() == subjectName.lowercase()) {
                sendMessage.text("Предмет с таким названием уже добавлен, попробуйте снова")
                    .replyMarkup(
                        ForceReplyKeyboard.builder()
                            .forceReply(true)
                            .selective(true)
                            .build()
                    )
                telegramService.client.execute(sendMessage.build())
                return false
            }
        }

        subjectService.save(
            Subject(
                name = subjectName,
                group = group
            )
        )

        sendMessage.text(
            """
            Предмет с названием "$subjectName" добавлен
            Список всех предметов - /list_subjects
        """.trimIndent()
        )

        telegramService.client.execute(sendMessage.build())
        return true
    }

    override fun scope() = Scope.GROUP
}