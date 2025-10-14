package me.alllexey123.itmoqueue.bot.callback

import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.bot.extensions.edit
import me.alllexey123.itmoqueue.bot.extensions.markdown
import me.alllexey123.itmoqueue.bot.extensions.toThread
import me.alllexey123.itmoqueue.bot.extensions.withTextAndInlineKeyboard
import me.alllexey123.itmoqueue.bot.view.SubjectView
import me.alllexey123.itmoqueue.model.Group
import me.alllexey123.itmoqueue.model.ManagedMessage
import me.alllexey123.itmoqueue.model.Subject
import me.alllexey123.itmoqueue.model.enums.MessageType
import me.alllexey123.itmoqueue.services.ManagedMessageService
import me.alllexey123.itmoqueue.services.QueueService
import me.alllexey123.itmoqueue.services.Telegram
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
        val activeEntries = queueService.sortedEntries(subject, !group.settings!!.attemptsEnabled).filter { !it.done }
        val text = subjectView.getSubjectText(subject, activeEntries)
        val keyboard = subjectView.getSubjectKeyboard()

        val send = SendMessage.builder()
            .toThread(group.settings!!.mainThreadId ?: threadId)
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
        val activeEntries = queueService.sortedEntries(subject, !group.settings!!.attemptsEnabled).filter { !it.done }
        val text = subjectView.getSubjectText(subject, activeEntries)
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