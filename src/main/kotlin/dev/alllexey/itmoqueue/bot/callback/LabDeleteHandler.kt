package dev.alllexey.itmoqueue.bot.callback

import dev.alllexey.itmoqueue.bot.Scope
import dev.alllexey.itmoqueue.bot.callback.CallbackUtils.Companion.USER_LABS_KEY
import dev.alllexey.itmoqueue.bot.extensions.getBoolean
import dev.alllexey.itmoqueue.bot.extensions.markdown
import dev.alllexey.itmoqueue.bot.extensions.withTextAndInlineKeyboard
import dev.alllexey.itmoqueue.bot.view.LabDeleteView
import dev.alllexey.itmoqueue.model.enums.MessageType
import dev.alllexey.itmoqueue.services.LabService
import dev.alllexey.itmoqueue.services.ManagedMessageService
import dev.alllexey.itmoqueue.services.Telegram
import org.springframework.stereotype.Component

@Component
class LabDeleteHandler(
    private val labDeleteView: LabDeleteView,
    private val callbackUtils: CallbackUtils,
    private val managedMessageService: ManagedMessageService,
    private val telegram: Telegram,
    private val labListHandler: LabListHandler,
    private val labService: LabService
) :
    CallbackHandler, ICallbackUtils by callbackUtils {
    override fun handleCallback(context: CallbackContext) {
        when (context.data) {
            is CallbackData.LabDeleteAsk -> handleAsk(context)
            is CallbackData.LabDeleteCancel -> handleCancel(context)
            is CallbackData.LabDeleteConfirm -> handleConfirm(context)
            else -> {}
        }
    }

    private fun handleConfirm(context: CallbackContext) {
        if (!requireMetadataAdmin(context)) return
        val lab = getLabOrDelete(context) ?: return
        labService.deleteById(lab.id!!)
        context.answer("Лаба успешно удалена")
        val parent = getParentMessageOrDelete(context) ?: return
        context.deleteMessage()

        if (parent.messageType != MessageType.LAB_DETAILS) return
        if (parent.metadata.getBoolean(USER_LABS_KEY, false)) {
            labListHandler.updateUserListMessage(context.user, parent)
        } else {
            labListHandler.updateGroupListMessage(lab.group, parent)
        }
    }

    private fun handleCancel(context: CallbackContext) {
        if (!requireMetadataAdmin(context)) return
        context.deleteMessage()
    }

    private fun handleAsk(context: CallbackContext) {
        if (!requireMetadataAdmin(context)) return
        val lab = getLabOrDelete(context) ?: return
        val text = labDeleteView.getLabDeleteText()
        val keyboard = labDeleteView.getLabDeleteKeyboard()
        val send = context.send(lab.group.settings.mainThreadId)
            .markdown()
            .withTextAndInlineKeyboard(text, keyboard)

        managedMessageService.register(
            sentMessage = telegram.execute(send),
            type = MessageType.LAB_DELETE_CONFIRM,
            metadata = mergeMetadata(labMetadata(lab), parentMessageMetadata(context.managedMessage)),
        )
    }

    override fun prefix() = "lab_delete"

    override fun scope() = Scope.ANY
}