package me.alllexey123.itmoqueue.bot.callback

import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.bot.extensions.edit
import me.alllexey123.itmoqueue.bot.extensions.markdown
import me.alllexey123.itmoqueue.bot.extensions.withTextAndInlineKeyboard
import me.alllexey123.itmoqueue.bot.view.NewView
import me.alllexey123.itmoqueue.model.Group
import me.alllexey123.itmoqueue.model.ManagedMessage
import me.alllexey123.itmoqueue.model.enums.MessageType
import me.alllexey123.itmoqueue.services.ManagedMessageService
import me.alllexey123.itmoqueue.services.Telegram
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage

@Component
class NewHandler(
    private val callbackUtils: CallbackUtils,
    private val telegram: Telegram,
    private val managedMessageService: ManagedMessageService,
    private val newView: NewView
) : CallbackHandler, ICallbackUtils by callbackUtils {

    override fun handleCallback(context: CallbackContext) {
        when (context.data) {
            is CallbackData.NewMenu -> handleMenu(context)
            else -> {}
        }
    }

    private fun handleMenu(context: CallbackContext) {
        if (!requireMetadataAdmin(context)) return
        val group = getGroupOrDelete(context) ?: return
        updateMenu(context.managedMessage, group)
    }

    fun sendMenu(group: Group) {
        val text = newView.getMenuText()
        val keyboard = newView.getMenuKeyboard()
        val send = SendMessage.builder()
            .chatId(group.chatId)
            .markdown()
            .text(text)
            .replyMarkup(keyboard)

        group.settings.mainThreadId?.let { send.messageThreadId(it) }

        managedMessageService.register(
            sentMessage = telegram.execute(send.build()),
            type = MessageType.NEW_MENU,
            metadata = groupMetadata(group)
        )
    }

    fun updateMenu(managedMessage: ManagedMessage, group: Group) {
        val text = newView.getMenuText()
        val keyboard = newView.getMenuKeyboard()
        val edit = managedMessage.edit()
            .markdown()
            .withTextAndInlineKeyboard(text, keyboard)
        telegram.execute(edit)
        managedMessage.metadata += groupMetadata(group)
    }


    override fun prefix() = "new"

    override fun scope() = Scope.ANY

}