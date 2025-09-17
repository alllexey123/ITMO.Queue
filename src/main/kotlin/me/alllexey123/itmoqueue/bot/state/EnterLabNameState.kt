package me.alllexey123.itmoqueue.bot.state

import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.bot.Validators
import me.alllexey123.itmoqueue.bot.extensions.replyTo
import me.alllexey123.itmoqueue.model.LabWork
import me.alllexey123.itmoqueue.services.GroupService
import me.alllexey123.itmoqueue.services.LabWorkService
import me.alllexey123.itmoqueue.services.SubjectService
import me.alllexey123.itmoqueue.services.TelegramService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.message.Message

@Component
class EnterLabNameState(
    private val groupService: GroupService,
    private val telegramService: TelegramService,
    private val subjectService: SubjectService,
    private val validators: Validators,
    private val labWorkService: LabWorkService
) : StateHandler() {

    // data in format "subject [subjectId]"
    override fun handle(message: Message): Boolean {
        val chat = message.chat
        val labName = message.text.trim()
        val group = groupService.getOrCreateByChatId(chat.id)
        val data = getChatData(chat.id)?.split(" ")

        if (data == null) return true
        val subject = subjectService.findById(data[1].toLong())
        if (subject == null) return true

        val sendMessage = SendMessage.builder()
            .replyTo(message)

        val check = validators.checkLabName(labName, sendMessage, group)
        if (!check) return false

        labWorkService.save(
            LabWork(
                name = labName,
                group = group,
                subject = subject
            )
        )

        sendMessage.text(
            """
            Лаба "$labName" по предмету "${subject.name}" создана
            Список - /list_labs
        """.trimIndent()
        )

        telegramService.client.execute(sendMessage.build())
        return true
    }

    override fun scope() = Scope.GROUP
}