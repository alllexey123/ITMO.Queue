package me.alllexey123.itmoqueue.bot.state

import me.alllexey123.itmoqueue.bot.MessageContext
import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.bot.ValidationResult
import me.alllexey123.itmoqueue.bot.Validators
import me.alllexey123.itmoqueue.bot.callback.CallbackData
import me.alllexey123.itmoqueue.bot.callback.CallbackUtils
import me.alllexey123.itmoqueue.bot.callback.ICallbackUtils
import me.alllexey123.itmoqueue.bot.command.Command
import me.alllexey123.itmoqueue.bot.extensions.inlineButton
import me.alllexey123.itmoqueue.bot.extensions.row
import me.alllexey123.itmoqueue.model.Membership
import me.alllexey123.itmoqueue.model.enums.MessageType
import me.alllexey123.itmoqueue.services.GroupService
import me.alllexey123.itmoqueue.services.LabService
import me.alllexey123.itmoqueue.services.ManagedMessageService
import me.alllexey123.itmoqueue.services.MembershipService
import me.alllexey123.itmoqueue.services.SubjectService
import me.alllexey123.itmoqueue.services.Telegram
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup

@Component
class EnterLabNameState(
    private val telegram: Telegram,
    private val subjectService: SubjectService,
    private val validators: Validators,
    private val labService: LabService,
    private val membershipService: MembershipService,
    private val groupService: GroupService,
    private val managedMessageService: ManagedMessageService,
    private val callbackUtils: CallbackUtils
) : StateHandler(), ICallbackUtils by callbackUtils {

    override fun handle(context: MessageContext): Boolean {
        val labName = context.text.trim()

        val group = getChatData(context.chatId)?.getOrNull(0)?.let { groupService.findById(it.toLong()) }
        if (group == null) return true
        if (membershipService.findByGroupAndUser(group, context.user)?.type != Membership.Type.ADMIN) {
            return context.isPrivate
        }

        val subjectId = getChatData(context.chatId)?.getOrNull(1)?.toLong()

        val subject = subjectService.findById(subjectId)
        if (subject == null) return true

        val sendMessage = context.sendReply()

        val check = validators.checkLabName(labName, group)
        if (check is ValidationResult.Failure){
            sendMessage.text(check.msg)
            telegram.execute(sendMessage.build())
            return false
        }

        val lab = labService.create(group, labName, subject)

        sendMessage.text(
            """
            Лаба "$labName" по предмету "${subject.name}" создана
            Список - ${Command.LABS.escaped}
        """.trimIndent()
        )

        if (context.isPrivate) {
            sendMessage.replyMarkup(InlineKeyboardMarkup(listOf(row(sendToGroup(lab.id!!)))))
        }

        managedMessageService.register(
            sentMessage = telegram.execute(sendMessage.build()),
            type = MessageType.LAB_CREATED,
            metadata = labMetadata(lab)
        )
        return true
    }

    companion object Buttons {
        val sendToGroup = { labId: Long -> inlineButton("Отправить в группу", CallbackData.NewLabSendToGroup(labId)) }
    }

    override fun scope() = Scope.GROUP
}