package me.alllexey123.itmoqueue.bot.callback

import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.bot.extensions.edit
import me.alllexey123.itmoqueue.bot.extensions.markdown
import me.alllexey123.itmoqueue.bot.extensions.withTextAndInlineKeyboard
import me.alllexey123.itmoqueue.bot.state.EnterLabNameState
import me.alllexey123.itmoqueue.bot.state.StateManager
import me.alllexey123.itmoqueue.bot.view.NewLabView
import me.alllexey123.itmoqueue.model.Group
import me.alllexey123.itmoqueue.model.ManagedMessage
import me.alllexey123.itmoqueue.services.Telegram
import org.springframework.stereotype.Component

@Component
class NewLabHandler(
    private val callbackUtils: CallbackUtils,
    private val telegram: Telegram,
    private val newLabView: NewLabView,
    private val enterLabNameState: EnterLabNameState,
    private val stateManager: StateManager,
    private val labHandler: LabHandler
) : CallbackHandler, ICallbackUtils by callbackUtils {

    override fun handleCallback(context: CallbackContext) {
        when (val data = context.data) {
            is CallbackData.NewLabSelectSubject -> handleSelectSubject(context, data.subjectId)
            is CallbackData.NewLabSelectSubjectPage -> handleSelectSubjectPage(context, data.page)
            is CallbackData.NewLabSendToGroup -> handleSendToGroup(context, data.labId)
            else -> {}
        }
    }

    fun handleSelectSubject(context: CallbackContext, subjectId: Long) {
        if (!requireMetadataAdmin(context)) return
        val subject = getSubjectOrDelete(context, subjectId) ?: return
        val group = getGroupOrDelete(context) ?: return
        updateEnterLabName(context.managedMessage, subject.id!!, group)
    }

    fun handleSelectSubjectPage(context: CallbackContext, page: Int = 0) {
        if (!requireMetadataAdmin(context)) return
        val group = getGroupOrDelete(context) ?: return
        updateSelectSubjectList(context.managedMessage, group, page)
    }

    fun handleSendToGroup(context: CallbackContext, labId: Long) {
        val lab = getLabOrDelete(context, labId) ?: return
        labHandler.sendLabDetails(lab.group, lab, true)
        context.answer("Сообщение отправлено!")
        context.deleteMessage()
    }

    fun updateEnterLabName(managedMessage: ManagedMessage, subjectId: Long, group: Group) {
        val text = newLabView.getNewLabEnterNameText()
        val keyboard = newLabView.getNewLabEnterNameKeyboard()
        val edit = managedMessage.edit()
            .markdown()
            .withTextAndInlineKeyboard(text, keyboard)

        enterLabNameState.setChatData(managedMessage.id.chatId, group.id, subjectId)
        stateManager.setHandler(managedMessage.id.chatId, enterLabNameState)

        telegram.execute(edit)
    }

    fun updateSelectSubjectList(managedMessage: ManagedMessage, group: Group, page: Int = 0) {
        val text = if (group.subjects.isEmpty()) newLabView.getNewLabNoSubjectsText() else newLabView.getNewLabSelectSubjectText()
        val keyboard = newLabView.getNewLabSelectSubjectKeyboard(group.subjects, page)
        val edit = managedMessage.edit()
            .markdown()
            .withTextAndInlineKeyboard(text, keyboard)
        telegram.execute(edit)
        managedMessage.metadata += groupMetadata(group)
    }


    override fun prefix() = "new_lab"

    override fun scope() = Scope.ANY

}