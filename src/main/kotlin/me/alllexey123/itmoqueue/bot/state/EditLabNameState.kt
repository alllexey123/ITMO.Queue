package me.alllexey123.itmoqueue.bot.state

import me.alllexey123.itmoqueue.bot.MessageContext
import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.bot.ValidationResult
import me.alllexey123.itmoqueue.bot.Validators
import me.alllexey123.itmoqueue.bot.command.GroupListLabsCommand
import me.alllexey123.itmoqueue.services.LabService
import me.alllexey123.itmoqueue.services.Telegram
import org.springframework.stereotype.Component

@Component
class EditLabNameState(
    private val telegram: Telegram,
    private val validators: Validators,
    private val labService: LabService
) : StateHandler() {

    override fun handle(context: MessageContext): Boolean {
        if (!context.requireAdmin(telegram)) return false
        val labName = context.text.trim()
        val group = context.group!!

        val labId = getChatData(context.chatId)?.getOrNull(0)?.toLong()

        val lab = labService.findById(labId)
        if (lab == null) return true

        val check = validators.checkLabName(labName, group)
        val send = context.sendReply()
        if (check is ValidationResult.Failure) {
            send.text(check.msg)
            telegram.execute(send.build())
            return false
        }

        val prev = lab.name
        lab.name = labName

        send.text(
            """
            Название изменено с "$prev" на "$labName".
            Список - /${GroupListLabsCommand.NAME}
        """.trimIndent()
        )

        telegram.execute(send.build())
        return true
    }

    override fun scope() = Scope.GROUP
}