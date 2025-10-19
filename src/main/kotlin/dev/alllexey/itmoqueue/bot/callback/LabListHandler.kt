package dev.alllexey.itmoqueue.bot.callback

import dev.alllexey.itmoqueue.bot.Scope
import dev.alllexey.itmoqueue.bot.callback.CallbackUtils.Companion.LAB_LIST_PAGE_KEY
import dev.alllexey.itmoqueue.bot.callback.CallbackUtils.Companion.USER_LABS_KEY
import dev.alllexey.itmoqueue.bot.extensions.edit
import dev.alllexey.itmoqueue.bot.extensions.getBoolean
import dev.alllexey.itmoqueue.bot.extensions.getInt
import dev.alllexey.itmoqueue.bot.extensions.markdown
import dev.alllexey.itmoqueue.bot.extensions.toThread
import dev.alllexey.itmoqueue.bot.extensions.withTextAndInlineKeyboard
import dev.alllexey.itmoqueue.bot.view.LabListView
import dev.alllexey.itmoqueue.model.Group
import dev.alllexey.itmoqueue.model.ManagedMessage
import dev.alllexey.itmoqueue.model.QueueEntry
import dev.alllexey.itmoqueue.model.User
import dev.alllexey.itmoqueue.model.enums.MessageType
import dev.alllexey.itmoqueue.services.ManagedMessageService
import dev.alllexey.itmoqueue.services.Telegram
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage

@Component
class LabListHandler(
    private val callbackUtils: CallbackUtils,
    private val telegram: Telegram,
    private val managedMessageService: ManagedMessageService,
    private val labListView: LabListView
) : CallbackHandler, ICallbackUtils by callbackUtils {
    override fun handleCallback(context: CallbackContext) {
        when (val data = context.data) {
            is CallbackData.ShowLabList -> handle(context, data.page)
            else -> {}
        }
    }

    private fun handle(context: CallbackContext, page: Int) {
        if (context.managedMessage.metadata.getBoolean(USER_LABS_KEY, false)) {
            updateUserListMessage(context.user, context.managedMessage, page)
        } else {
            val group = context.group ?: getGroupOrDelete(context) ?: return
            updateGroupListMessage(group, context.managedMessage, page)
        }
    }

    fun sendGroupList(group: Group, page: Int = 0, threadId: Int? = null) {
        val labs = group.labs
        val text = labListView.getLabsListGroupText(labs, page)
        val keyboard = labListView.getLabsListGroupKeyboard(labs, page)
        val send = SendMessage.builder()
            .toThread(group.settings.mainThreadId ?: threadId)
            .markdown()
            .chatId(group.chatId)
            .text(text)
            .replyMarkup(keyboard)

        managedMessageService.register(
            sentMessage = telegram.execute(send.build()),
            type = MessageType.LAB_DETAILS,
            metadata = groupMetadata(group).also {
                it[LAB_LIST_PAGE_KEY] = page
                it[USER_LABS_KEY] = false
            }
        )
    }

    fun updateGroupListMessage(group: Group, managedMessage: ManagedMessage, page: Int? = null) {
        val realPage = page ?: managedMessage.metadata.getInt(LAB_LIST_PAGE_KEY, 0)
        val labs = group.labs
        val text = labListView.getLabsListGroupText(labs, realPage)
        val keyboard = labListView.getLabsListGroupKeyboard(labs, realPage)
        val editMessage = managedMessage.edit()
            .markdown()
            .withTextAndInlineKeyboard(text, keyboard)
        telegram.execute(editMessage)
        managedMessage.metadata[LAB_LIST_PAGE_KEY] = realPage
        managedMessage.metadata[USER_LABS_KEY] = false
        managedMessage.messageType = MessageType.GROUP_LAB_LIST
    }

    fun sendUserList(user: User, page: Int = 0) {
        val entries = getUserUniqueEntries(user)
        val text = labListView.getLabsListUserText(entries, page)
        val keyboard = labListView.getLabsListUserKeyboard(entries, page)

        val send = SendMessage.builder()
            .markdown()
            .chatId(user.telegramId)
            .text(text)
            .replyMarkup(keyboard)

        managedMessageService.register(
            sentMessage = telegram.execute(send.build()),
            type = MessageType.LAB_DETAILS,
            metadata = userMetadata(user).also {
                it[LAB_LIST_PAGE_KEY] = page
                it[USER_LABS_KEY] = true
            }
        )
    }

    fun updateUserListMessage(user: User, managedMessage: ManagedMessage, page: Int? = null) {
        val realPage = page ?: managedMessage.metadata.getInt(LAB_LIST_PAGE_KEY, 0)
        val entries = getUserUniqueEntries(user)
        val text = labListView.getLabsListUserText(entries, realPage)
        val keyboard = labListView.getLabsListUserKeyboard(entries, realPage)
        val editMessage = managedMessage.edit()
            .markdown()
            .withTextAndInlineKeyboard(text, keyboard)
        telegram.execute(editMessage)
        managedMessage.metadata[LAB_LIST_PAGE_KEY] = realPage
        managedMessage.metadata[USER_LABS_KEY] = true
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

    override fun prefix() = "lab_list"

    override fun scope() = Scope.ANY
}