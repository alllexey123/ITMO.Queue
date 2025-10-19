package dev.alllexey.itmoqueue.bot.callback

import dev.alllexey.itmoqueue.bot.Scope
import dev.alllexey.itmoqueue.bot.extensions.edit
import dev.alllexey.itmoqueue.bot.extensions.markdown
import dev.alllexey.itmoqueue.bot.extensions.withTextAndInlineKeyboard
import dev.alllexey.itmoqueue.bot.view.SubjectQueueTypeView
import dev.alllexey.itmoqueue.model.enums.MergedQueueType
import dev.alllexey.itmoqueue.services.Telegram
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup

@Component
class SubjectQueueTypeHandler(
    private val callbackUtils: CallbackUtils,
    private val telegram: Telegram,
    private val subjectHandler: SubjectHandler,
    private val subjectQueueTypeView: SubjectQueueTypeView,
    private val groupSettingsHandler: GroupSettingsHandler
) : CallbackHandler, ICallbackUtils by callbackUtils {

    override fun handleCallback(context: CallbackContext) {
        when (val data = context.data) {
            is CallbackData.SubjectQueueTypeAsk -> handleAsk(context, data.selectingDefault)
            is CallbackData.SubjectQueueTypeSelect -> handleSelect(context, data.selectingDefault, data.type)
            else -> {}
        }
    }

    private fun handleAsk(context: CallbackContext, selectingDefault: Boolean) {
        if (!requireMetadataAdmin(context)) return
        val group = getGroupOrDelete(context) ?: return
        val text: String
        val keyboard: InlineKeyboardMarkup
        if (selectingDefault) {
            text = subjectQueueTypeView.getDefaultQueueTypeText(group)
            keyboard = subjectQueueTypeView.getSubjectQueueTypeKeyboard(true)
        } else {
            val subject = getSubjectOrDelete(context) ?: return
            text = subjectQueueTypeView.getSubjectQueueTypeText(subject)
            keyboard = subjectQueueTypeView.getSubjectQueueTypeKeyboard(false)
        }
        val edit = context.managedMessage.edit()
            .markdown()
            .withTextAndInlineKeyboard(text, keyboard)

        telegram.execute(edit)
    }

    private fun handleSelect(context: CallbackContext, selectingDefault: Boolean, type: MergedQueueType) {
        if (!requireMetadataAdmin(context)) return
        val group = getGroupOrDelete(context) ?: return
        if (selectingDefault) {
            group.settings.defaultMergedQueueType = type
            groupSettingsHandler.updateGroupSettings(group, context.managedMessage, context.isPrivate)
        } else {
            val subject = getSubjectOrDelete(context) ?: return
            subject.mergedQueueType = type
            subjectHandler.updateSubjectDetails(subject, context.managedMessage)
        }
    }

    override fun prefix() = "subject_queue_type"

    override fun scope() = Scope.ANY

}