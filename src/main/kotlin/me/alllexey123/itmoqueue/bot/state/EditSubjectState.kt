package me.alllexey123.itmoqueue.bot.state

import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.bot.ValidationResult
import me.alllexey123.itmoqueue.bot.Validators
import me.alllexey123.itmoqueue.bot.extensions.replyTo
import me.alllexey123.itmoqueue.services.GroupService
import me.alllexey123.itmoqueue.services.SubjectService
import me.alllexey123.itmoqueue.services.Telegram
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.message.Message

@Component
class EditSubjectState(
    private val groupService: GroupService,
    private val telegram: Telegram,
    private val subjectService: SubjectService,
    private val validators: Validators
) : StateHandler() {

    override fun handle(message: Message): Boolean {
        val chat = message.chat
        val subjectName = message.text.trim()
        val group = groupService.getOrCreateByChatId(chat.id)
        val subjectId = getChatData(chat.id)?.toLong()

        if (subjectId == null) return true
        val subject = subjectService.findById(subjectId)
        if (subject == null) return true

        val sendMessage = SendMessage.builder()
            .replyTo(message)

        val check = validators.checkSubjectName(subjectName, group)
        if (check is ValidationResult.Failure) {
            sendMessage.text(check.msg)
            telegram.execute(sendMessage.build())
            return false
        }

        val prev = subject.name
        subject.name = subjectName

        sendMessage.text(
            """
            Название изменено с "$prev" на "$subjectName".
            Список - /list_subjects
        """.trimIndent()
        )

        telegram.execute(sendMessage.build())
        return true
    }

    override fun scope() = Scope.GROUP
}