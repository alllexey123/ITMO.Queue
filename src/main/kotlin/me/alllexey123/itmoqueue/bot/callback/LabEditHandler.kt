package me.alllexey123.itmoqueue.bot.callback

import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.bot.extensions.edit
import me.alllexey123.itmoqueue.bot.extensions.markdown
import me.alllexey123.itmoqueue.bot.extensions.withTextAndInlineKeyboard
import me.alllexey123.itmoqueue.bot.state.EditLabNameState
import me.alllexey123.itmoqueue.bot.state.StateManager
import me.alllexey123.itmoqueue.bot.view.LabChangeNameView
import me.alllexey123.itmoqueue.bot.view.LabEditView
import me.alllexey123.itmoqueue.model.enums.MessageType
import me.alllexey123.itmoqueue.services.Telegram
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText

@Component
class LabEditHandler(
    private val callbackUtils: CallbackUtils,
    private val telegram: Telegram,
    private val labEditView: LabEditView,
    private val labChangeNameView: LabChangeNameView,
    private val editLabNameState: EditLabNameState,
    private val stateManager: StateManager
) : CallbackHandler, ICallbackUtils by callbackUtils {

    override fun handleCallback(context: CallbackContext) {
        when(context.data) {
            is CallbackData.LabEditChangeName -> handleChangeName(context)
            is CallbackData.LabEditMenu -> handleMenu(context)
            else -> {}
        }
    }

    private fun handleMenu(context: CallbackContext) {
        if (!requireMetadataAdmin(context)) return
        val lab = getLabOrDelete(context) ?: return
        val group = lab.group
        val text = labEditView.getLabEditText(lab, !group.settings.attemptsEnabled)
        val keyboard = labEditView.getLabEditKeyboard(!group.settings.attemptsEnabled)
        val edit = context.edit().markdown()
            .withTextAndInlineKeyboard(text, keyboard)
        telegram.execute(edit)
        context.managedMessage.messageType = MessageType.LAB_EDIT
    }

    private fun handleChangeName(context: CallbackContext) {
        if (!requireMetadataAdmin(context)) return
        val lab = getLabOrDelete(context) ?: return
        val text = labChangeNameView.getLabChangeNameText()
        val keyboard = labChangeNameView.getLabChangeNameKeyboard()
        val editMessage = EditMessageText.builder()
            .edit(context.message)
            .withTextAndInlineKeyboard(text, keyboard)

        editLabNameState.setChatData(context.chatId, lab.id!!)
        stateManager.setHandler(context.chatId, editLabNameState)
        telegram.execute(editMessage)
    }

    override fun prefix() = "lab_edit"

    override fun scope() = Scope.ANY
}