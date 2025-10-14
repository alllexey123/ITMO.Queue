package me.alllexey123.itmoqueue.bot.callback

import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.bot.extensions.edit
import me.alllexey123.itmoqueue.bot.extensions.markdown
import me.alllexey123.itmoqueue.bot.extensions.toThread
import me.alllexey123.itmoqueue.bot.extensions.withTextAndInlineKeyboard
import me.alllexey123.itmoqueue.bot.view.GroupListView
import me.alllexey123.itmoqueue.model.Group
import me.alllexey123.itmoqueue.model.ManagedMessage
import me.alllexey123.itmoqueue.model.User
import me.alllexey123.itmoqueue.model.enums.MessageType
import me.alllexey123.itmoqueue.services.ManagedMessageService
import me.alllexey123.itmoqueue.services.Telegram
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage

@Component
class GroupListHandler (
    private val callbackUtils: CallbackUtils,
    private val telegram: Telegram,
    private val managedMessageService: ManagedMessageService,
    private val groupListView: GroupListView
) : CallbackHandler, ICallbackUtils by callbackUtils {
    override fun handleCallback(context: CallbackContext) {
        when (val data = context.data) {
            is CallbackData.GroupListMenu -> handleMenu(context)
            is CallbackData.GroupListSelect -> handleSelect(context, data.groupId)
            is CallbackData.GroupListBack -> handleBack(context)
            else -> {}
        }
    }

    private fun handleMenu(context: CallbackContext) {
        updateGroupList(context.user, context.managedMessage)
    }

    private fun handleSelect(context: CallbackContext, groupId: Long) {
        val group = getGroupOrDelete(context, groupId) ?: return
        updateGroupMenu(group, context.managedMessage, context.isPrivate)
    }

    private fun handleBack(context: CallbackContext) {
        val group = getGroupOrDelete(context) ?: return
        updateGroupMenu(group, context.managedMessage, context.isPrivate)
    }

    fun sendGroupList(user: User) {
        val groups = user.memberships.map { it.group }
        val text = groupListView.getGroupListText(groups)
        val keyboard = groupListView.getGroupListKeyboard(groups)
        val send = SendMessage.builder()
            .chatId(user.telegramId)
            .markdown()
            .withTextAndInlineKeyboard(text, keyboard)

        managedMessageService.register(
            sentMessage = telegram.execute(send),
            type = MessageType.GROUP_LIST
        )
    }

    fun updateGroupList(user: User, managedMessage: ManagedMessage) {
        val groups = user.memberships.map { it.group }
        val text = groupListView.getGroupListText(groups)
        val keyboard = groupListView.getGroupListKeyboard(groups)
        val edit = managedMessage.edit()
            .markdown()
            .withTextAndInlineKeyboard(text, keyboard)

        telegram.execute(edit)
        managedMessage.messageType = MessageType.GROUP_LIST
    }

    fun sendGroupMenu(group: Group, threadId: Int? = null) {
        val text = groupListView.getGroupMenuText(group)
        val keyboard = groupListView.getGroupMenuKeyboard(false)
        val send = SendMessage.builder()
            .toThread(group.settings!!.mainThreadId ?: threadId)
            .chatId(group.chatId)
            .markdown()
            .text(text)
            .replyMarkup(keyboard)

        managedMessageService.register(
            sentMessage = telegram.execute(send.build()),
            type = MessageType.GROUP_MENU
        )
    }

    fun updateGroupMenu(group: Group, managedMessage: ManagedMessage, isPrivate: Boolean) {
        val text = groupListView.getGroupMenuText(group)
        val keyboard = groupListView.getGroupMenuKeyboard(isPrivate)
        val edit = managedMessage.edit()
            .markdown()
            .withTextAndInlineKeyboard(text, keyboard)

        telegram.execute(edit)
        managedMessage.metadata += groupMetadata(group)
        managedMessage.messageType = MessageType.GROUP_MENU
    }

    override fun prefix() = "group_list"

    override fun scope() = Scope.ANY

}