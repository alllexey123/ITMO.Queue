package me.alllexey123.itmoqueue.bot.state

import me.alllexey123.itmoqueue.bot.MessageContext
import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.bot.ValidationResult
import me.alllexey123.itmoqueue.bot.Validators
import me.alllexey123.itmoqueue.bot.command.GroupListSubjectsCommand
import me.alllexey123.itmoqueue.services.SubjectService
import me.alllexey123.itmoqueue.services.Telegram
import org.springframework.stereotype.Component

@Component
class EditSubjectState(
    private val telegram: Telegram,
    private val subjectService: SubjectService,
    private val validators: Validators
) : StateHandler() {

    override fun handle(context: MessageContext): Boolean {
        if (!context.requireAdmin(telegram)) return false
        val subjectName = context.text.trim()
        val group = context.group!!
        val subjectId = getChatData(context.chatId)?.getOrNull(0)?.toLong()

        val subject = subjectService.findById(subjectId)
        if (subject == null) return true

        val sendMessage = context.sendReply()

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
            Список - /${GroupListSubjectsCommand.NAME}
        """.trimIndent()
        )

        telegram.execute(sendMessage.build())
        return true
    }

    override fun scope() = Scope.GROUP
}