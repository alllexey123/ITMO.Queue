package me.alllexey123.itmoqueue.bot.callback

import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.bot.extensions.edit
import me.alllexey123.itmoqueue.bot.extensions.markdown
import me.alllexey123.itmoqueue.bot.extensions.withTextAndInlineKeyboard
import me.alllexey123.itmoqueue.bot.state.EnterSubjectNameState
import me.alllexey123.itmoqueue.bot.state.StateManager
import me.alllexey123.itmoqueue.bot.view.NewSubjectView
import me.alllexey123.itmoqueue.model.Group
import me.alllexey123.itmoqueue.model.ManagedMessage
import me.alllexey123.itmoqueue.services.Telegram
import org.springframework.stereotype.Component

@Component
class NewSubjectHandler(
    private val callbackUtils: CallbackUtils,
    private val telegram: Telegram,
    private val stateManager: StateManager,
    private val enterSubjectNameState: EnterSubjectNameState,
    private val newSubjectView: NewSubjectView
) : CallbackHandler, ICallbackUtils by callbackUtils {

    override fun handleCallback(context: CallbackContext) {
        when (context.data) {
            is CallbackData.NewSubjectMenu -> handleMenu(context)
            else -> {}
        }
    }

    fun handleMenu(context: CallbackContext) {
        if (!requireMetadataAdmin(context)) return
        val group = getGroupOrDelete(context) ?: return
        updateEnterSubjectName(context.managedMessage, group)
    }

    fun updateEnterSubjectName(managedMessage: ManagedMessage, group: Group) {
        val text = newSubjectView.getNewSubjectText()
        val keyboard = newSubjectView.getNewSubjectKeyboard()
        val edit = managedMessage.edit()
            .markdown()
            .withTextAndInlineKeyboard(text, keyboard)

        enterSubjectNameState.setChatData(managedMessage.id.chatId, group.id)
        stateManager.setHandler(managedMessage.id.chatId, enterSubjectNameState)

        telegram.execute(edit)
    }


    override fun prefix() = "new_subject"

    override fun scope() = Scope.ANY

}