package dev.alllexey.itmoqueue.bot.state

import dev.alllexey.itmoqueue.bot.MessageContext
import dev.alllexey.itmoqueue.bot.Scope
import dev.alllexey.itmoqueue.bot.ValidationResult
import dev.alllexey.itmoqueue.bot.Validators
import dev.alllexey.itmoqueue.bot.command.Command
import dev.alllexey.itmoqueue.model.Membership
import dev.alllexey.itmoqueue.services.MembershipService
import dev.alllexey.itmoqueue.services.SubjectService
import dev.alllexey.itmoqueue.services.Telegram
import org.springframework.stereotype.Component

@Component
class EditSubjectNameState(
    private val telegram: Telegram,
    private val subjectService: SubjectService,
    private val validators: Validators,
    private val membershipService: MembershipService
) : StateHandler() {

    override fun handle(context: MessageContext): Boolean {
        val subjectName = context.text.trim()
        val subjectId = getChatData(context.chatId)?.getOrNull(0)?.toLong()

        val subject = subjectService.findById(subjectId)
        if (subject == null) return true

        val group = subject.group
        if (membershipService.findByGroupAndUser(group, context.user)?.type != Membership.Type.ADMIN) {
            return context.isPrivate
        }

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
            Список - ${Command.SUBJECTS.escaped}
        """.trimIndent()
        )

        telegram.execute(sendMessage.build())
        return true
    }

    override fun scope() = Scope.GROUP
}