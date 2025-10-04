package me.alllexey123.itmoqueue.bot.state

import me.alllexey123.itmoqueue.bot.MessageContext
import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.bot.ValidationResult
import me.alllexey123.itmoqueue.bot.Validators
import me.alllexey123.itmoqueue.bot.command.GroupListLabsCommand
import me.alllexey123.itmoqueue.services.LabService
import me.alllexey123.itmoqueue.services.QueueService
import me.alllexey123.itmoqueue.services.SubjectService
import me.alllexey123.itmoqueue.services.Telegram
import org.springframework.stereotype.Component

@Component
class EnterLabNameState(
    private val telegram: Telegram,
    private val subjectService: SubjectService,
    private val validators: Validators,
    private val labService: LabService,
    private val queueService: QueueService
) : StateHandler() {

    // data in format "subject [subjectId]"
    override fun handle(context: MessageContext): Boolean {
        if (!context.requireAdmin(telegram)) return false
        val labName = context.text.trim()
        val group = context.group!!
        val subjectId = getChatData(context.chatId)?.getOrNull(0)?.toLong()

        val subject = subjectService.findById(subjectId)
        if (subject == null) return true

        val sendMessage = context.sendReply()

        val check = validators.checkLabName(labName, group)
        if (check is ValidationResult.Failure){
            sendMessage.text(check.msg)
            telegram.execute(sendMessage.build())
            return false
        }

        labService.create(group, labName, subject)

        sendMessage.text(
            """
            Лаба "$labName" по предмету "${subject.name}" создана
            Список - /${GroupListLabsCommand.NAME}
        """.trimIndent()
        )

        telegram.execute(sendMessage.build())
        return true
    }

    override fun scope() = Scope.GROUP
}