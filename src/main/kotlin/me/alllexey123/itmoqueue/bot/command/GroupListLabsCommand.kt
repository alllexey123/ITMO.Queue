package me.alllexey123.itmoqueue.bot.command

import me.alllexey123.itmoqueue.bot.Emoji
import me.alllexey123.itmoqueue.bot.MessageContext
import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.bot.callback.CallbackContext
import me.alllexey123.itmoqueue.bot.callback.CallbackData
import me.alllexey123.itmoqueue.bot.callback.CallbackDataSerializer
import me.alllexey123.itmoqueue.bot.callback.ICallbackDataSerializer
import me.alllexey123.itmoqueue.bot.extensions.*
import me.alllexey123.itmoqueue.bot.state.EditLabNameState
import me.alllexey123.itmoqueue.bot.state.StateManager
import me.alllexey123.itmoqueue.model.Group
import me.alllexey123.itmoqueue.model.Lab
import me.alllexey123.itmoqueue.model.ManagedMessage
import me.alllexey123.itmoqueue.model.enums.MessageType
import me.alllexey123.itmoqueue.services.*
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup

@Component
class GroupListLabsCommand(
    private val telegram: Telegram,
    private val labService: LabService,
    private val queueService: QueueService,
    private val callbackDataSerializer: CallbackDataSerializer,
    private val telegramViewService: TelegramViewService,
    private val managedMessageService: ManagedMessageService,
    private val editLabNameState: EditLabNameState,
    private val stateManager: StateManager,
) : AbstractListLabsCommand(telegram, labService, queueService, telegramViewService, managedMessageService),
    ICallbackDataSerializer by callbackDataSerializer {

    override fun handleCallback(context: CallbackContext) {
        super.handleCallback(context)
        when (context.data) {
            is CallbackData.DeleteLab -> handleDeleteLab(context)
            is CallbackData.EditLab -> handleEditLab(context)
            is CallbackData.PinLab -> handlePinLab(context)
            else -> {}
        }
    }

    fun handleDeleteLab(context: CallbackContext) {
        if (!context.requireAdmin()) return
        val lab = getLabOrDelete(context) ?: return
        labService.deleteById(lab.id!!)
    }

    fun handleEditLab(context: CallbackContext) {
        if (!context.requireAdmin()) return
        val lab = getLabOrDelete(context) ?: return
        val editMessage = EditMessageText.builder()
            .edit(context.message)
            .text("Введите новое название лабы (ответом на это сообщение):")
            .replyMarkup(
                InlineKeyboardMarkup.builder().keyboardRow(
                    inlineRowButton(Emoji.BACK, serialize(CallbackData.SelectLab(lab.id!!)))
                ).build()
            ).build()

        editLabNameState.setChatData(context.chatId, lab.id!!)
        stateManager.setHandler(context.chatId, editLabNameState)
        telegram.execute(editMessage)
    }

    fun handlePinLab(context: CallbackContext) {
        if (!context.requireAdmin()) return
        val lab = getLabOrDelete(context) ?: return
        updateLabDetails(lab, context.managedMessage!!, pinned = true)
    }

    override fun handleLabsList(context: CallbackContext) {
        updateListMessage(context.group!!, context.managedMessage!!)
    }

    override fun handleLabsListPage(context: CallbackContext, page: Int) {
        updateListMessage(context.group!!, context.managedMessage!!, page)
    }

    override fun handleSelectLab(context: CallbackContext, labId: Long) {
        val lab = getLabOrDelete(context, labId) ?: return
        updateLabDetails(lab, context.managedMessage!!, pinned = null)
    }

    override fun updateLabDetails(lab: Lab, managedMessage: ManagedMessage) {
        updateLabDetails(lab, managedMessage, null)
    }

    fun updateLabDetails(lab: Lab, managedMessage: ManagedMessage, pinned: Boolean?) {
        val realPinned = pinned ?: managedMessage.metadata.getBoolean("pinned", false)
        val activeEntries = lab.queueEntries.filter { !it.done }
        val text = telegramViewService.buildLabDetailsText(lab, activeEntries)
        val keyboard = telegramViewService.buildLabDetailsGroupKeyboard(lab, realPinned)
        val editMessage = managedMessage.edit()
            .parseMode(ParseMode.MARKDOWN)
            .withTextAndInlineKeyboard(text, keyboard)
        telegram.execute(editMessage)
        managedMessageService.touch(managedMessage)
        managedMessage.metadata["lab_id"] = lab.id!!
        managedMessage.metadata["pinned"] = realPinned
        managedMessage.messageType = MessageType.LAB_DETAILS
    }

    fun sendListMessage(context: MessageContext) {
        val labs = context.group!!.labs
        val text = telegramViewService.buildLabsListGroupText(labs, page = 1)
        val keyboard = telegramViewService.buildLabsListGroupKeyboard(labs, page = 1)
        val sendMessage = context.send()
            .withTextAndInlineKeyboard(text, keyboard)
        val sent = telegram.execute(sendMessage)
        managedMessageService.register(
            sent,
            type = MessageType.GROUP_LAB_LIST,
            metadata = mutableMapOf("labs_list_page" to 1)
        )
    }

    fun updateListMessage(group: Group, managedMessage: ManagedMessage, page: Int? = null) {
        val realPage = page ?: managedMessage.metadata.getInt("labs_list_page", 1)
        val labs = group.labs
        val text = telegramViewService.buildLabsListGroupText(labs, realPage)
        val keyboard = telegramViewService.buildLabsListGroupKeyboard(labs, realPage)
        val editMessage = managedMessage.edit()
            .withTextAndInlineKeyboard(text, keyboard)
        telegram.execute(editMessage)
        managedMessage.metadata["labs_list_page"] = realPage
        managedMessage.messageType = MessageType.GROUP_LAB_LIST
    }

    override fun handleMessage(context: MessageContext) {
        sendListMessage(context)
    }

    override fun scope() = Scope.GROUP

    companion object {
        const val NAME = "list_labs"
    }
}