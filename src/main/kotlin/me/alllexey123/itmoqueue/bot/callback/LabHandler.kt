package me.alllexey123.itmoqueue.bot.callback

import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.bot.callback.CallbackUtils.Companion.LAB_PINNED_KEY
import me.alllexey123.itmoqueue.bot.extensions.edit
import me.alllexey123.itmoqueue.bot.extensions.getBoolean
import me.alllexey123.itmoqueue.bot.extensions.markdown
import me.alllexey123.itmoqueue.bot.extensions.toThread
import me.alllexey123.itmoqueue.bot.extensions.withTextAndInlineKeyboard
import me.alllexey123.itmoqueue.bot.view.LabView
import me.alllexey123.itmoqueue.model.Group
import me.alllexey123.itmoqueue.model.Lab
import me.alllexey123.itmoqueue.model.ManagedMessage
import me.alllexey123.itmoqueue.model.enums.MessageType
import me.alllexey123.itmoqueue.services.ManagedMessageService
import me.alllexey123.itmoqueue.services.QueueService
import me.alllexey123.itmoqueue.services.Telegram
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import java.time.Duration
import java.time.Instant

@Component
class LabHandler(
    private val callbackUtils: CallbackUtils,
    private val queueService: QueueService,
    private val labView: LabView,
    private val telegram: Telegram,
    private val managedMessageService: ManagedMessageService
) : CallbackHandler, ICallbackUtils by callbackUtils {

    override fun handleCallback(context: CallbackContext) {
        when (val data = context.data) {
            is CallbackData.LabDetailsRefresh -> handleDetailsRefresh(context)
            is CallbackData.LabDetailsShow -> handleDetailsShow(context, data.labId)
            is CallbackData.LabPin -> handlePin(context)
            is CallbackData.LabMarkEntryDone -> handleMarkQueueEntryDone(context)
            is CallbackData.LabRemoveFromQueue -> handleRemoveFromQueue(context)
            else -> {}
        }
    }

    private fun handleDetailsRefresh(context: CallbackContext) {
        val lab = getLabOrDelete(context) ?: return
        if (context.managedMessage.messageType == MessageType.LAB_DETAILS && Duration.between(context.managedMessage.updatedAt, Instant.now()).toSeconds() < 1) {
            context.answer("Не так быстро!")
            return
        }
        updateLabDetails(lab, context.managedMessage)
    }

    private fun handleDetailsShow(context: CallbackContext, labId: Long) {
        val lab = getLabOrDelete(context, labId) ?: return
        updateLabDetails(lab, context.managedMessage)
    }

    private fun handlePin(context: CallbackContext) {
        val lab = getLabOrDelete(context) ?: return
        if (!requireMetadataAdmin(context)) return
        updateLabDetails(lab, context.managedMessage, true)
    }

    private fun handleRemoveFromQueue(context: CallbackContext) {
        val lab = getLabOrDelete(context) ?: return

        if (queueService.removeUserFromQueue(context.user, lab)) {
            context.answer("Вы удалены из очереди")
            updateLabDetails(lab, context.managedMessage)
        } else {
            context.answer("Вас нет в этой очереди")
        }
    }

    private fun handleMarkQueueEntryDone(context: CallbackContext) {
        val lab = getLabOrDelete(context) ?: return
        val entry = queueService.findEntryByLabAndUserAndDone(lab, context.user, false)

        if (entry == null) {
            context.answer("Вас нет в этой очереди")
        } else {
            entry.markDone()
            context.answer("Ваша позиция отмечена как завершённая")
            updateLabDetails(lab, context.managedMessage)
        }
    }

    fun sendLabDetails(group: Group, lab: Lab, pinned: Boolean = false, threadId: Int? = null) {
        val activeEntries = queueService.sortedEntries(lab, !group.settings!!.attemptsEnabled).filter { !it.done }
        val text = labView.getLabText(lab, activeEntries)
        val keyboard = labView.getLabKeyboard(
            hideSettings = pinned,
            hideNavigation = pinned
        )

        val send = SendMessage.builder()
            .toThread(group.settings!!.mainThreadId ?: threadId)
            .chatId(group.chatId)
            .markdown()
            .text(text)
            .replyMarkup(keyboard)

        managedMessageService.register(
            sentMessage = telegram.execute(send.build()),
            type = MessageType.LAB_DETAILS,
            metadata = labMetadata(lab).also { it[LAB_PINNED_KEY] = pinned }
        )
    }

    fun updateLabDetails(lab: Lab, managedMessage: ManagedMessage, pinned: Boolean? = null) {
        val group = lab.group
        val realPinned = pinned ?: managedMessage.metadata.getBoolean(LAB_PINNED_KEY, false)
        val activeEntries = queueService.sortedEntries(lab, !group.settings!!.attemptsEnabled).filter { !it.done }
        val text = labView.getLabText(lab, activeEntries)
        val keyboard = labView.getLabKeyboard(
            hideSettings = realPinned,
            hideNavigation = realPinned
        )

        val editMessage = managedMessage.edit()
            .markdown()
            .withTextAndInlineKeyboard(text, keyboard)

        telegram.execute(editMessage)
        managedMessageService.touch(managedMessage)
        managedMessage.metadata += labMetadata(lab)
        managedMessage.metadata[LAB_PINNED_KEY] = realPinned
        managedMessage.messageType = MessageType.LAB_DETAILS
    }

    override fun prefix() = "lab"

    override fun scope() = Scope.ANY
}