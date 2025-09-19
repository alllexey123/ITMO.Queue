package me.alllexey123.itmoqueue.bot.state

import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.bot.ValidationResult
import me.alllexey123.itmoqueue.bot.Validators
import me.alllexey123.itmoqueue.bot.extensions.replyTo
import me.alllexey123.itmoqueue.services.GroupService
import me.alllexey123.itmoqueue.services.LabWorkService
import me.alllexey123.itmoqueue.services.Telegram
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.message.Message

@Component
class EditLabNameState(
    private val groupService: GroupService,
    private val telegram: Telegram,
    private val validators: Validators,
    private val labWorkService: LabWorkService
) : StateHandler() {

    override fun handle(message: Message): Boolean {
        val chat = message.chat
        val labName = message.text.trim()
        val group = groupService.getOrCreateByChatId(chat.id)

        val labId = getChatData(chat.id)?.toLong()

        if (labId == null) return true
        val lab = labWorkService.findById(labId)
        if (lab == null) return true

        val check = validators.checkLabName(labName, group)
        val sendMessage = SendMessage.builder()
            .replyTo(message)
        if (check is ValidationResult.Failure) {
            sendMessage.text(check.msg)
            telegram.execute(sendMessage.build())
            return false
        }

        val prev = lab.name
        lab.name = labName

        sendMessage.text(
            """
            Название изменено с "$prev" на "$labName".
            Список - /list_labs
        """.trimIndent()
        )

        telegram.execute(sendMessage.build())
        return true
    }

    override fun scope() = Scope.GROUP
}