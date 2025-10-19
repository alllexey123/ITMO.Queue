package dev.alllexey.itmoqueue.bot.callback

import dev.alllexey.itmoqueue.bot.Scope
import dev.alllexey.itmoqueue.bot.extensions.edit
import dev.alllexey.itmoqueue.bot.extensions.markdown
import dev.alllexey.itmoqueue.bot.extensions.withTextAndInlineKeyboard
import dev.alllexey.itmoqueue.bot.state.EnterSubjectNameState
import dev.alllexey.itmoqueue.bot.state.StateManager
import dev.alllexey.itmoqueue.bot.view.NewSubjectView
import dev.alllexey.itmoqueue.model.Group
import dev.alllexey.itmoqueue.model.ManagedMessage
import dev.alllexey.itmoqueue.services.Telegram
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