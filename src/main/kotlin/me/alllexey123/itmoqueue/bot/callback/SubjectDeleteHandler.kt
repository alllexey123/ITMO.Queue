package me.alllexey123.itmoqueue.bot.callback

import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.bot.extensions.markdown
import me.alllexey123.itmoqueue.bot.extensions.withTextAndInlineKeyboard
import me.alllexey123.itmoqueue.bot.view.SubjectDeleteView
import me.alllexey123.itmoqueue.model.enums.MessageType
import me.alllexey123.itmoqueue.services.SubjectService
import me.alllexey123.itmoqueue.services.ManagedMessageService
import me.alllexey123.itmoqueue.services.Telegram
import org.springframework.stereotype.Component

@Component
class SubjectDeleteHandler(
    private val subjectDeleteView: SubjectDeleteView,
    private val callbackUtils: CallbackUtils,
    private val managedMessageService: ManagedMessageService,
    private val telegram: Telegram,
    private val subjectService: SubjectService,
    private val subjectHandler: SubjectHandler
) :
    CallbackHandler, ICallbackUtils by callbackUtils {
    override fun handleCallback(context: CallbackContext) {
        when (context.data) {
            is CallbackData.SubjectDeleteAsk -> handleAsk(context)
            is CallbackData.SubjectDeleteCancel -> handleCancel(context)
            is CallbackData.SubjectDeleteConfirm -> handleConfirm(context)
            else -> {}
        }
    }

    private fun handleConfirm(context: CallbackContext) {
        if (!requireMetadataAdmin(context)) return
        val subject = getSubjectOrDelete(context) ?: return
        subjectService.deleteById(subject.id!!)
        context.answer("Предмет успешно удалён")
        val parent = getParentMessageOrDelete(context) ?: return
        context.deleteMessage()

        if (parent.messageType != MessageType.SUBJECT_DETAILS) return
        subjectHandler.updateSubjectDetails(subject, parent)
    }

    private fun handleCancel(context: CallbackContext) {
        if (!requireMetadataAdmin(context)) return
        context.deleteMessage()
    }

    private fun handleAsk(context: CallbackContext) {
        if (!requireMetadataAdmin(context)) return
        val subject = getSubjectOrDelete(context) ?: return
        val text = subjectDeleteView.getSubjectDeleteText()
        val keyboard = subjectDeleteView.getSubjectDeleteKeyboard()
        val send = context.send(subject.group.settings.mainThreadId)
            .markdown()
            .withTextAndInlineKeyboard(text, keyboard)

        managedMessageService.register(
            sentMessage = telegram.execute(send),
            type = MessageType.SUBJECT_DELETE_CONFIRM,
            metadata = mergeMetadata(subjectMetadata(subject), parentMessageMetadata(context.managedMessage)),
        )
    }

    override fun prefix() = "subject_delete"

    override fun scope() = Scope.ANY
}