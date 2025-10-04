package me.alllexey123.itmoqueue.bot.command

import me.alllexey123.itmoqueue.bot.callback.CallbackContext
import me.alllexey123.itmoqueue.bot.callback.CallbackData
import me.alllexey123.itmoqueue.bot.callback.CallbackHandler
import me.alllexey123.itmoqueue.bot.extensions.getInt
import me.alllexey123.itmoqueue.bot.extensions.getLong
import me.alllexey123.itmoqueue.bot.extensions.getLongOrNull
import me.alllexey123.itmoqueue.model.Lab
import me.alllexey123.itmoqueue.model.ManagedMessage
import me.alllexey123.itmoqueue.model.enums.MessageType
import me.alllexey123.itmoqueue.services.LabService
import me.alllexey123.itmoqueue.services.ManagedMessageService
import me.alllexey123.itmoqueue.services.QueueService
import me.alllexey123.itmoqueue.services.Telegram
import me.alllexey123.itmoqueue.services.TelegramViewService
import org.telegram.telegrambots.meta.api.methods.ParseMode
import java.time.Duration
import java.time.Instant

abstract class AbstractListLabsCommand(
    private val telegram: Telegram,
    private val labService: LabService,
    private val queueService: QueueService,
    private val telegramViewService: TelegramViewService,
    private val managedMessageService: ManagedMessageService,
) : CommandHandler, CallbackHandler {

    override fun command() = NAME

    override fun prefix() = NAME

    override fun handleCallback(context: CallbackContext) {
        when (val data = context.data) {
            is CallbackData.AddToQueue -> handleAddToQueue(context)
            is CallbackData.AddToQueueCancel -> handleAddToQueueCancel(context)
            is CallbackData.MarkQueueEntryDone -> handleMarkQueueEntryDone(context)
            is CallbackData.RemoveFromQueue -> handleRemoveFromQueue(context)
            is CallbackData.SelectAttemptNumber -> handleSelectAttemptNumber(context, data.attempt)
            is CallbackData.SelectLab -> handleSelectLab(context, data.labId)
            is CallbackData.ShowLabsList -> handleLabsList(context)
            is CallbackData.ShowLabsPage -> handleLabsListPage(context, data.page)
            else -> {}
        }
    }

    abstract fun handleLabsList(context: CallbackContext)

    abstract fun handleLabsListPage(context: CallbackContext, page: Int)

    abstract fun handleSelectLab(context: CallbackContext, labId: Long)

    abstract fun updateLabDetails(lab: Lab, managedMessage: ManagedMessage)

    fun handleAddToQueue(context: CallbackContext) {
        val lab = getLabOrDelete(context) ?: return
        if (queueService.hasActiveEntry(context.user, lab)) {
            val answer = context.answer("Вы уже присутствуете в очереди")
            telegram.execute(answer)
        } else {
            val sendAttemptMessage = context.send()
                .text(telegramViewService.buildLabAttemptText(context.user, context.isPrivate))
                .parseMode(ParseMode.MARKDOWN)
                .replyMarkup(telegramViewService.buildLabAttemptKeyboard())
                .build()
            val sentMessage = telegram.execute(sendAttemptMessage)
            managedMessageService.register(
                sentMessage = sentMessage,
                type = MessageType.LAB_ATTEMPT_SELECT,
                metadata = mutableMapOf("lab_id" to lab.id!!, "user_id" to context.user.id!!, "main_message_id" to context.messageId)
            )
        }
    }

    fun handleAddToQueueCancel(context: CallbackContext) {
        val lab = getLabOrDelete(context) ?: return
        val MIN_SECONDS = 60
        val managedMessage = context.managedMessage!!
        val userId = managedMessage.metadata.getLong("user_id")
        val seconds = Duration.between(managedMessage.createdAt, Instant.now()).toSeconds()
        if (context.user.id != userId && seconds < MIN_SECONDS) {
            context.answer("Это сообщение можно будет удалить через ${MIN_SECONDS - seconds} секунд.")
        } else {
            val mainMessageId = managedMessage.metadata.getInt("main_message_id")
            val mainMessage = managedMessageService.findById(context.chatId, mainMessageId)
            if (mainMessage?.messageType == MessageType.LAB_DETAILS) updateLabDetails(lab, mainMessage)
            context.answer("Сообщение удалено.")
            context.deleteMessage()
        }
    }

    fun handleMarkQueueEntryDone(context: CallbackContext) {
        val lab = getLabOrDelete(context) ?: return
        val entry = queueService.findEntryByLabAndUserAndDone(lab, context.user, false)

        if (entry == null) {
            context.answer("У вас нет активных позиций в этой очереди")
        } else {
            entry.done = true
            entry.markedDoneAt = Instant.now()
            context.answer("Ваша позиция отмечена как завершённая")
            updateLabDetails(lab, context.managedMessage!!)
        }
    }

    fun handleRemoveFromQueue(context: CallbackContext) {
        val lab = getLabOrDelete(context) ?: return

        if (queueService.removeUserFromQueue(context.user, lab)) {
            context.answer("Вы успешно удалены из очереди")
            updateLabDetails(lab, context.managedMessage!!)
        } else {
            context.answer("Вас нет в этой очереди")
        }
    }

    fun handleSelectAttemptNumber(context: CallbackContext, attempt: Int) {
        val lab = getLabOrDelete(context) ?: return
        val managedMessage = context.managedMessage!!
        val userId = managedMessage.metadata.getLong("user_id")
        val mainMessageId = managedMessage.metadata.getInt("main_message_id")

        if (context.user.id != userId) {
            context.answer("Вы не можете выбрать за другого человека")
            return
        }

        if (queueService.hasActiveEntry(context.user, lab)) {
            context.answer("Вы уже присутствуете в очереди")
        } else {
            queueService.addToQueue(context.user, lab, attempt)
            context.answer("Вы добавлены в очередь")
            val mainMessage = managedMessageService.findById(context.chatId, mainMessageId)
            if (mainMessage?.messageType == MessageType.LAB_DETAILS) updateLabDetails(lab, mainMessage)
        }
    }

    fun getLabOrDelete(context: CallbackContext, labId: Long? = null): Lab? {
        val managedMessage = context.managedMessage
        val lab = labService.findById(labId ?: managedMessage?.metadata?.getLongOrNull("lab_id"))
        if (lab == null) {
            context.deleteMessage()
            return null
        }
        return lab
    }

    companion object {
        const val NAME = "list_labs"
    }
}