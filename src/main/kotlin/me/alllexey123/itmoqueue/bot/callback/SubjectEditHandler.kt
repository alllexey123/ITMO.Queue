package me.alllexey123.itmoqueue.bot.callback

import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.bot.extensions.edit
import me.alllexey123.itmoqueue.bot.extensions.markdown
import me.alllexey123.itmoqueue.bot.extensions.withTextAndInlineKeyboard
import me.alllexey123.itmoqueue.bot.state.EditSubjectNameState
import me.alllexey123.itmoqueue.bot.state.StateManager
import me.alllexey123.itmoqueue.bot.view.SubjectChangeNameView
import me.alllexey123.itmoqueue.bot.view.SubjectEditView
import me.alllexey123.itmoqueue.model.enums.MessageType
import me.alllexey123.itmoqueue.services.Telegram
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText

@Component
class SubjectEditHandler(
    private val callbackUtils: CallbackUtils,
    private val telegram: Telegram,
    private val subjectEditView: SubjectEditView,
    private val subjectChangeNameView: SubjectChangeNameView,
    private val editSubjectNameState: EditSubjectNameState,
    private val stateManager: StateManager
) : CallbackHandler, ICallbackUtils by callbackUtils {

    override fun handleCallback(context: CallbackContext) {
        when(context.data) {
            is CallbackData.SubjectEditChangeName -> handleChangeName(context)
            is CallbackData.SubjectEditMenu -> handleMenu(context)
            else -> {}
        }
    }

    private fun handleMenu(context: CallbackContext) {
        if (!requireMetadataAdmin(context)) return
        val subject = getSubjectOrDelete(context) ?: return
        val group = subject.group
        val text = subjectEditView.getSubjectEditText(subject, !group.settings.attemptsEnabled)
        val keyboard = subjectEditView.getSubjectEditKeyboard(!group.settings.attemptsEnabled)
        val edit = context.edit().markdown()
            .withTextAndInlineKeyboard(text, keyboard)
        telegram.execute(edit)
        context.managedMessage.messageType = MessageType.SUBJECT_EDIT
    }

    private fun handleChangeName(context: CallbackContext) {
        if (!requireMetadataAdmin(context)) return
        val subject = getSubjectOrDelete(context) ?: return
        val text = subjectChangeNameView.getSubjectChangeNameText()
        val keyboard = subjectChangeNameView.getSubjectChangeNameKeyboard()
        val editMessage = EditMessageText.builder()
            .edit(context.message)
            .withTextAndInlineKeyboard(text, keyboard)

        editSubjectNameState.setChatData(context.chatId, subject.id!!)
        stateManager.setHandler(context.chatId, editSubjectNameState)
        telegram.execute(editMessage)
    }

    override fun prefix() = "subject_edit"

    override fun scope() = Scope.ANY
}