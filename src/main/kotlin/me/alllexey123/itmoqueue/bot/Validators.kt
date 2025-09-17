package me.alllexey123.itmoqueue.bot

import me.alllexey123.itmoqueue.bot.extensions.withForceReply
import me.alllexey123.itmoqueue.model.Group
import me.alllexey123.itmoqueue.services.TelegramService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage

@Component
class Validators(private val telegramService: TelegramService) {

    fun checkSubjectName(
        subjectName: String,
        sendMessage: SendMessage.SendMessageBuilder<*, *>,
        group: Group
    ): Boolean {

        if (subjectName.length > 20) {
            val msg = sendMessage.withForceReply("Название предмета длиннее 20 символов, попробуйте снова")
            telegramService.client.execute(msg)
            return false
        }

        if (group.subjects.any { subject -> subject.name == subjectName }) {
            val msg = sendMessage.withForceReply("Предмет с таким названием уже добавлен, попробуйте снова")
            telegramService.client.execute(msg)
            return false
        }

        return true
    }

    fun checkLabName(
        labName: String,
        sendMessage: SendMessage.SendMessageBuilder<*, *>,
        group: Group
    ): Boolean {

        if (labName.length > 20) {
            val msg = sendMessage.withForceReply("Название лабы длиннее 20 символов, попробуйте снова")
            telegramService.client.execute(msg)
            return false
        }

        if (group.labs.any { work -> work.name == labName }) {
            val msg = sendMessage.withForceReply("Лаба с таким названием уже добавлена, попробуйте снова")
            telegramService.client.execute(msg)
            return false
        }

        return true
    }

}