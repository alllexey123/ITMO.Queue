package dev.alllexey.itmoqueue.bot.state

import dev.alllexey.itmoqueue.bot.MessageContext
import dev.alllexey.itmoqueue.bot.Scope
import dev.alllexey.itmoqueue.bot.ValidationResult
import dev.alllexey.itmoqueue.bot.Validators
import dev.alllexey.itmoqueue.bot.command.Command
import dev.alllexey.itmoqueue.model.Membership
import dev.alllexey.itmoqueue.services.GroupService
import dev.alllexey.itmoqueue.services.MembershipService
import dev.alllexey.itmoqueue.services.SubjectService
import dev.alllexey.itmoqueue.services.Telegram
import org.springframework.stereotype.Component

@Component
class EnterSubjectNameState(
    private val groupService: GroupService,
    private val telegram: Telegram,
    private val subjectService: SubjectService,
    private val validators: Validators,
    private val membershipService: MembershipService
) : StateHandler() {

    override fun handle(context: MessageContext): Boolean {
        val subjectName = context.text.trim()
        val group = getChatData(context.chatId)?.getOrNull(0)?.let { groupService.findById(it.toLong()) }
        if (group == null) return true
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

        subjectService.create(group, subjectName)

        sendMessage.text(
            """
            Предмет "$subjectName" добавлен
            Список - ${Command.SUBJECTS.escaped}
        """.trimIndent()
        )

        telegram.execute(sendMessage.build())
        return true
    }

    override fun scope() = Scope.GROUP
}