package me.alllexey123.itmoqueue.bot.command

import me.alllexey123.itmoqueue.bot.MessageContext
import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.bot.callback.CallbackContext
import me.alllexey123.itmoqueue.bot.callback.CallbackDataSerializer
import me.alllexey123.itmoqueue.bot.callback.ICallbackDataSerializer
import me.alllexey123.itmoqueue.bot.extensions.edit
import me.alllexey123.itmoqueue.bot.extensions.getInt
import me.alllexey123.itmoqueue.bot.extensions.withTextAndInlineKeyboard
import me.alllexey123.itmoqueue.model.Lab
import me.alllexey123.itmoqueue.model.ManagedMessage
import me.alllexey123.itmoqueue.model.QueueEntry
import me.alllexey123.itmoqueue.model.User
import me.alllexey123.itmoqueue.model.enums.MessageType
import me.alllexey123.itmoqueue.services.LabService
import me.alllexey123.itmoqueue.services.ManagedMessageService
import me.alllexey123.itmoqueue.services.QueueService
import me.alllexey123.itmoqueue.services.Telegram
import me.alllexey123.itmoqueue.services.TelegramViewService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.ParseMode

@Component
class UserListLabsCommand(
    private val telegram: Telegram,
    private val labService: LabService,
    private val queueService: QueueService,
    private val callbackDataSerializer: CallbackDataSerializer,
    private val telegramViewService: TelegramViewService,
    private val managedMessageService: ManagedMessageService,
) : AbstractListLabsCommand(telegram, labService, queueService, telegramViewService, managedMessageService),
    ICallbackDataSerializer by callbackDataSerializer {

    override fun handleLabsList(context: CallbackContext) {
        val managedMessage = context.managedMessage!!
        updateListMessage(context.user, managedMessage)
    }

    override fun handleLabsListPage(context: CallbackContext, page: Int) {
        val managedMessage = context.managedMessage!!
        updateListMessage(context.user, managedMessage, page)
    }

    override fun handleSelectLab(context: CallbackContext, labId: Long) {
        val lab = getLabOrDelete(context, labId) ?: return
        val managedMessage = context.managedMessage!!
        updateLabDetails(lab, managedMessage)
    }

    override fun updateLabDetails(lab: Lab, managedMessage: ManagedMessage) {
        val activeEntries = lab.queueEntries.filter { !it.done }
        val text = telegramViewService.buildLabDetailsText(lab, activeEntries)
        val keyboard = telegramViewService.buildLabDetailUserKeyboard(lab)
        val editMessage = managedMessage.edit()
            .parseMode(ParseMode.MARKDOWN)
            .withTextAndInlineKeyboard(text, keyboard)
        telegram.execute(editMessage)
        managedMessageService.touch(managedMessage)
        managedMessage.metadata["lab_id"] = lab.id!!
        managedMessage.messageType = MessageType.LAB_DETAILS
    }

    override fun handleMessage(context: MessageContext) {
        sendListMessage(context)
    }

    fun sendListMessage(context: MessageContext) {
        val entries = getUserUniqueEntries(context.user)
        val text = telegramViewService.buildLabsListUserText(entries, page = 1)
        val keyboard = telegramViewService.buildLabsListUserKeyboard(entries, page = 1)
        val sendMessage = context.send()
            .parseMode(ParseMode.MARKDOWN)
            .withTextAndInlineKeyboard(text, keyboard)
        val sent = telegram.execute(sendMessage)
        managedMessageService.register(
            sent,
            type = MessageType.USER_LAB_LIST,
            metadata = mutableMapOf("labs_list_page" to 1)
        )
    }

    fun updateListMessage(user: User, managedMessage: ManagedMessage, page: Int? = null) {
        val entries = getUserUniqueEntries(user)
        val realPage = page ?: managedMessage.metadata.getInt("labs_list_page", 1)
        val text = telegramViewService.buildLabsListUserText(entries, realPage)
        val keyboard = telegramViewService.buildLabsListUserKeyboard(entries, realPage)
        val editMessage = managedMessage.edit()
            .withTextAndInlineKeyboard(text, keyboard)
        telegram.execute(editMessage)
        managedMessage.metadata["labs_list_page"] = realPage
        managedMessage.messageType = MessageType.USER_LAB_LIST
    }

    fun getUserUniqueEntries(user: User): List<QueueEntry> {
        return user.queueEntries
            .groupBy { it.lab }
            .values
            .mapNotNull { entriesForOneLab ->
                entriesForOneLab.firstOrNull { !it.done } ?: entriesForOneLab.lastOrNull()
            }
    }

    override fun scope() = Scope.USER

    companion object {
        const val NAME = "list_labs"
    }
}