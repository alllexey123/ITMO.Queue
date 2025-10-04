package me.alllexey123.itmoqueue.bot.command

import me.alllexey123.itmoqueue.bot.Emoji
import me.alllexey123.itmoqueue.bot.MessageContext
import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.bot.callback.*
import me.alllexey123.itmoqueue.bot.extensions.*
import me.alllexey123.itmoqueue.bot.state.EditSubjectState
import me.alllexey123.itmoqueue.bot.state.StateManager
import me.alllexey123.itmoqueue.model.Group
import me.alllexey123.itmoqueue.model.ManagedMessage
import me.alllexey123.itmoqueue.model.Subject
import me.alllexey123.itmoqueue.model.enums.MessageType
import me.alllexey123.itmoqueue.services.ManagedMessageService
import me.alllexey123.itmoqueue.services.SubjectService
import me.alllexey123.itmoqueue.services.Telegram
import me.alllexey123.itmoqueue.services.TelegramViewService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup

@Component
class GroupListSubjectsCommand(
    private val telegram: Telegram,
    private val subjectService: SubjectService,
    private val editSubjectState: EditSubjectState,
    private val stateManager: StateManager,
    private val telegramViewService: TelegramViewService,
    private val managedMessageService: ManagedMessageService,
    private val callbackDataSerializer: CallbackDataSerializer,
) : CommandHandler, CallbackHandler, ICallbackDataSerializer by callbackDataSerializer {

    override fun handleMessage(context: MessageContext) {
        sendListMessage(context)
    }

    override fun handleCallback(context: CallbackContext) {
        when (val data = context.data) {
            is CallbackData.DeleteSubject -> handleDeleteSubject(context)
            is CallbackData.EditSubject -> handleEditSubject(context)
            is CallbackData.SelectSubject -> handleSelectSubject(context, data.subjectId)
            is CallbackData.ShowSubjectsList -> handleListSubjects(context)
            is CallbackData.ShowSubjectsPage -> handleSubjectsListPage(context, data.page)
            else -> {}
        }
    }

    fun handleDeleteSubject(context: CallbackContext) {
        if (!context.requireAdmin()) return
        val subject = getSubjectOrDelete(context) ?: return
        subjectService.deleteById(subject.id!!)
        updateListMessage(context.group!!, context.managedMessage!!)
    }

    fun handleEditSubject(context: CallbackContext) {
        if (!context.requireAdmin()) return
        val subject = getSubjectOrDelete(context) ?: return
        val editMessage = EditMessageText.builder()
            .edit(context.message)
            .text("Введите новое название предмета (ответом на это сообщение):")
            .replyMarkup(
                InlineKeyboardMarkup.builder().keyboardRow(
                    inlineRowButton(Emoji.BACK, serialize(CallbackData.SelectSubject(subject.id!!)))
                ).build()
            )
            .build()

        editSubjectState.setChatData(context.chatId, subject.id)
        stateManager.setHandler(context.chatId, editSubjectState)
        telegram.execute(editMessage)
    }

    fun handleSelectSubject(context: CallbackContext, subjectId: Long) {
        val subject = getSubjectOrDelete(context, subjectId) ?: return

        updateSubjectDetails(subject, context.managedMessage!!)
    }

    fun handleListSubjects(context: CallbackContext) {
        updateListMessage(context.group!!, context.managedMessage!!)
    }

    fun handleSubjectsListPage(context: CallbackContext, page: Int) {
        updateListMessage(context.group!!, context.managedMessage!!, page)
    }

    fun updateSubjectDetails(subject: Subject, managedMessage: ManagedMessage) {
        val text = telegramViewService.buildSubjectDetailsText(subject)
        val keyboard = telegramViewService.buildSubjectDetailsKeyboard(subject)
        val editMessage = managedMessage.edit()
            .parseMode(ParseMode.MARKDOWN)
            .withTextAndInlineKeyboard(text, keyboard)
        telegram.execute(editMessage)
        managedMessage.metadata["subject_id"] = subject.id!!
        managedMessage.messageType = MessageType.SUBJECT_DETAILS
    }

    fun updateListMessage(group: Group, managedMessage: ManagedMessage, page: Int? = null) {
        val realPage = page ?: managedMessage.metadata.getInt("subjects_list_page", 1)
        val subjects = group.subjects
        val text = telegramViewService.buildSubjectsListText(subjects, realPage)
        val keyboard = telegramViewService.buildSubjectsListKeyboard(subjects, realPage)
        val editMessage = managedMessage.edit()
            .withTextAndInlineKeyboard(text, keyboard)
        telegram.execute(editMessage)
        managedMessage.metadata["subjects_list_page"] = realPage
        managedMessage.messageType = MessageType.SUBJECT_LIST
    }

    fun getSubjectOrDelete(context: CallbackContext, subjectId: Long? = null): Subject? {
        val managedMessage = context.managedMessage
        val lab = subjectService.findById(subjectId ?: managedMessage?.metadata?.getLongOrNull("subject_id"))
        if (lab == null) {
            context.deleteMessage()
            return null
        }
        return lab
    }

    fun sendListMessage(context: MessageContext) {
        val group = context.group!!
        val subjects = group.subjects
        val text = telegramViewService.buildSubjectsListText(subjects)
        val keyboard = telegramViewService.buildSubjectsListKeyboard(subjects)
        val sendMessage = context.send()
            .withTextAndInlineKeyboard(text, keyboard)

        val sent = telegram.execute(sendMessage)
        managedMessageService.register(
            sent,
            type = MessageType.SUBJECT_LIST,
            metadata = mutableMapOf("subjects_list_page" to 1)
        )
    }

    override fun prefix() = command()

    override fun command() = NAME

    override fun scope() = Scope.GROUP

    companion object {
        const val NAME = "list_subjects"
    }
}