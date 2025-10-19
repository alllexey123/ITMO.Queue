package dev.alllexey.itmoqueue.bot.callback

import dev.alllexey.itmoqueue.bot.Scope
import dev.alllexey.itmoqueue.bot.extensions.edit
import dev.alllexey.itmoqueue.bot.extensions.markdown
import dev.alllexey.itmoqueue.bot.extensions.withTextAndInlineKeyboard
import dev.alllexey.itmoqueue.bot.view.LabQueueTypeView
import dev.alllexey.itmoqueue.model.enums.QueueType
import dev.alllexey.itmoqueue.services.Telegram
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup

@Component
class LabQueueTypeHandler(
    private val callbackUtils: CallbackUtils,
    private val telegram: Telegram,
    private val labHandler: LabHandler,
    private val labQueueTypeView: LabQueueTypeView,
    private val groupSettingsHandler: GroupSettingsHandler
) : CallbackHandler, ICallbackUtils by callbackUtils {

    override fun handleCallback(context: CallbackContext) {
        when (val data = context.data) {
            is CallbackData.LabQueueTypeAsk -> handleAsk(context, data.selectingDefault)
            is CallbackData.LabQueueTypeSelect -> handleSelect(context, data.selectingDefault, data.type)
            else -> {}
        }
    }

    private fun handleAsk(context: CallbackContext, selectingDefault: Boolean) {
        if (!requireMetadataAdmin(context)) return
        val group = getGroupOrDelete(context) ?: return
        val text: String
        val keyboard: InlineKeyboardMarkup
        if (selectingDefault) {
            text = labQueueTypeView.getDefaultQueueTypeText(group)
            keyboard = labQueueTypeView.getLabQueueTypeKeyboard(true)
        } else {
            val lab = getLabOrDelete(context) ?: return
            text = labQueueTypeView.getLabQueueTypeText(lab)
            keyboard = labQueueTypeView.getLabQueueTypeKeyboard(false)
        }
        val edit = context.managedMessage.edit()
            .markdown()
            .withTextAndInlineKeyboard(text, keyboard)

        telegram.execute(edit)
    }

    private fun handleSelect(context: CallbackContext, selectingDefault: Boolean, type: QueueType) {
        if (!requireMetadataAdmin(context)) return
        val group = getGroupOrDelete(context) ?: return
        if (selectingDefault) {
            group.settings.defaultQueueType = type
            groupSettingsHandler.updateGroupSettings(group, context.managedMessage, context.isPrivate)
        } else {
            val lab = getLabOrDelete(context) ?: return
            lab.queueType = type
            labHandler.updateLabDetails(lab, context.managedMessage)
        }
    }

    override fun prefix() = "lab_queue_type"

    override fun scope() = Scope.ANY

}