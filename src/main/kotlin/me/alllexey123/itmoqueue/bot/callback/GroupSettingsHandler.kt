package me.alllexey123.itmoqueue.bot.callback

import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.bot.extensions.edit
import me.alllexey123.itmoqueue.bot.extensions.markdown
import me.alllexey123.itmoqueue.bot.extensions.withTextAndInlineKeyboard
import me.alllexey123.itmoqueue.bot.view.GroupSettingsView
import me.alllexey123.itmoqueue.model.Group
import me.alllexey123.itmoqueue.model.ManagedMessage
import me.alllexey123.itmoqueue.model.enums.MessageType
import me.alllexey123.itmoqueue.services.ManagedMessageService
import me.alllexey123.itmoqueue.services.Telegram
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.message.Message

@Component
class GroupSettingsHandler(
    private val callbackUtils: CallbackUtils,
    private val telegram: Telegram,
    private val groupSettingsView: GroupSettingsView,
    private val managedMessageService: ManagedMessageService
) : CallbackHandler, ICallbackUtils by callbackUtils {

    override fun handleCallback(context: CallbackContext) {
        when (val data = context.data) {
            is CallbackData.SettingsMenu -> handleMenu(context)
            is CallbackData.SettingsResetThread -> handleResetThread(context)
            is CallbackData.SettingsSelectThread -> handleSelectThread(context)
            is CallbackData.SettingsSwitchSetting -> handleSwitchSetting(context, data.setting, data.newVal)
            else -> {}
        }
    }

    private fun handleMenu(context: CallbackContext) {
        if (!requireMetadataAdmin(context)) return
        val group = getGroupOrDelete(context) ?: return
        updateGroupSettings(group, context.managedMessage, context.isPrivate, context.isTopicMessage())
    }

    private fun handleResetThread(context: CallbackContext) {
        if (!requireMetadataAdmin(context)) return
        val group = getGroupOrDelete(context) ?: return
        group.settings!!.mainThreadId = null
        updateGroupSettings(group, context.managedMessage, context.isPrivate, context.isTopicMessage())
    }

    private fun handleSelectThread(context: CallbackContext) {
        if (!requireMetadataAdmin(context)) return
        if (!context.isTopicMessage()) return
        val group = getGroupOrDelete(context) ?: return
        val message = context.message as Message
        group.settings!!.mainThreadId = message.messageThreadId
        updateGroupSettings(group, context.managedMessage, context.isPrivate, context.isTopicMessage())
    }

    private fun handleSwitchSetting(
        context: CallbackContext,
        setting: GroupSettingsView.SwitchSetting,
        newVal: Boolean
    ) {
        if (!requireMetadataAdmin(context)) return
        val group = getGroupOrDelete(context) ?: return
        when (setting) {
            GroupSettingsView.SwitchSetting.ATTEMPTS_ENABLED -> group.settings!!.attemptsEnabled = newVal
            GroupSettingsView.SwitchSetting.ASK_ATTEMPTS_DIRECTLY -> group.settings!!.askAttemptsDirectly = newVal
        }
        updateGroupSettings(group, context.managedMessage, context.isTopicMessage())
    }

    fun sendGroupSettings(group: Group, isTopic: Boolean) {
        val text = groupSettingsView.getGroupSettingsText(group)
        val keyboard = groupSettingsView.getGroupSettingsKeyboard(group, isTopic)
        val send = SendMessage.builder()
            .chatId(group.chatId)
            .markdown()
            .withTextAndInlineKeyboard(text, keyboard)

        managedMessageService.register(
            sentMessage = telegram.execute(send),
            type = MessageType.GROUP_SETTINGS,
            metadata = groupMetadata(group)
        )
    }

    fun updateGroupSettings(group: Group, managedMessage: ManagedMessage, isPrivate: Boolean, isTopic: Boolean = false) {
        val text = groupSettingsView.getGroupSettingsText(group)
        val keyboard = groupSettingsView.getGroupSettingsKeyboard(group, showThreadId = !isPrivate && isTopic)
        val edit = managedMessage.edit()
            .markdown()
            .withTextAndInlineKeyboard(text, keyboard)

        telegram.execute(edit)
        managedMessage.metadata += groupMetadata(group)
        managedMessage.messageType = MessageType.GROUP_SETTINGS
    }

    override fun prefix() = "settings"

    override fun scope() = Scope.ANY
}