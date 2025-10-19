package dev.alllexey.itmoqueue.bot.callback

import dev.alllexey.itmoqueue.bot.Scope
import dev.alllexey.itmoqueue.bot.extensions.edit
import dev.alllexey.itmoqueue.bot.extensions.markdown
import dev.alllexey.itmoqueue.bot.extensions.toThread
import dev.alllexey.itmoqueue.bot.extensions.withTextAndInlineKeyboard
import dev.alllexey.itmoqueue.bot.view.SubjectView
import dev.alllexey.itmoqueue.model.Group
import dev.alllexey.itmoqueue.model.ManagedMessage
import dev.alllexey.itmoqueue.model.Subject
import dev.alllexey.itmoqueue.model.enums.MessageType
import dev.alllexey.itmoqueue.services.ManagedMessageService
import dev.alllexey.itmoqueue.services.QueueService
import dev.alllexey.itmoqueue.services.Telegram
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage

@Component
class SubjectHandler(
    private val callbackUtils: CallbackUtils,
    private val queueService: QueueService,
    private val subjectView: SubjectView,
    private val telegram: Telegram,
    private val managedMessageService: ManagedMessageService
) : CallbackHandler, ICallbackUtils by callbackUtils {

    override fun handleCallback(context: CallbackContext) {
        when (val data = context.data) {
            is CallbackData.SubjectDetailsRefresh -> handleDetailsRefresh(context)
            is CallbackData.SubjectDetailsShow -> handleDetailsShow(context, data.subjectId)
            is CallbackData.SubjectShowLabList -> handleShowLabList(context)
            else -> {}
        }
    }

    private fun handleDetailsRefresh(context: CallbackContext) {
        val subject = getSubjectOrDelete(context) ?: return
        updateSubjectDetails(subject, context.managedMessage)
    }

    private fun handleDetailsShow(context: CallbackContext, subjectId: Long) {
        val subject = getSubjectOrDelete(context, subjectId) ?: return
        updateSubjectDetails(subject, context.managedMessage)
    }

    private fun handleShowLabList(context: CallbackContext) {
        val subject = getSubjectOrDelete(context) ?: return
        updateSubjectLabList(subject, context.managedMessage)
    }

    fun sendSubjectDetails(group: Group, subject: Subject, threadId: Int? = null) {
        val activeEntries = queueService.sortedEntries(subject, !group.settings.attemptsEnabled).filter { !it.done }
        val text = subjectView.getSubjectText(subject, activeEntries, !group.settings.attemptsEnabled)
        val keyboard = subjectView.getSubjectKeyboard()

        val send = SendMessage.builder()
            .toThread(group.settings.mainThreadId ?: threadId)
            .chatId(group.chatId)
            .markdown()
            .text(text)
            .replyMarkup(keyboard)

        managedMessageService.register(
            sentMessage = telegram.execute(send.build()),
            type = MessageType.SUBJECT_DETAILS,
            metadata = subjectMetadata(subject)
        )
    }

    fun updateSubjectDetails(subject: Subject, managedMessage: ManagedMessage) {
        val group = subject.group
        val activeEntries = queueService.sortedEntries(subject, !group.settings.attemptsEnabled).filter { !it.done }
        val text = subjectView.getSubjectText(subject, activeEntries, !group.settings.attemptsEnabled)
        val keyboard = subjectView.getSubjectKeyboard()

        val editMessage = managedMessage.edit()
            .markdown()
            .withTextAndInlineKeyboard(text, keyboard)

        telegram.execute(editMessage)
        managedMessage.metadata += subjectMetadata(subject)
        managedMessage.messageType = MessageType.SUBJECT_DETAILS
    }

    fun updateSubjectLabList(subject: Subject, managedMessage: ManagedMessage) {
        val text = subjectView.getSubjectLabListText(subject)
        val keyboard = subjectView.getSubjectLabListKeyboard()
        val editMessage = managedMessage.edit()
            .markdown()
            .withTextAndInlineKeyboard(text, keyboard)
        telegram.execute(editMessage)
        managedMessage.metadata += subjectMetadata(subject)
        managedMessage.messageType = MessageType.SUBJECT_LAB_LIST
    }

    override fun prefix() = "subject"

    override fun scope() = Scope.ANY
}