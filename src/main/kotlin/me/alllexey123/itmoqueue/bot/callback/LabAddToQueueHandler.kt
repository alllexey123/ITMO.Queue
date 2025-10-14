package me.alllexey123.itmoqueue.bot.callback

import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.bot.view.LabAddToQueueView
import me.alllexey123.itmoqueue.model.enums.MessageType
import me.alllexey123.itmoqueue.services.ManagedMessageService
import me.alllexey123.itmoqueue.services.QueueService
import me.alllexey123.itmoqueue.services.Telegram
import org.springframework.stereotype.Component
import me.alllexey123.itmoqueue.bot.extensions.*
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import java.time.Duration
import java.time.Instant

@Component
class LabAddToQueueHandler(
    private val callbackUtils: CallbackUtils,
    private val queueService: QueueService,
    private val telegram: Telegram,
    private val labAddToQueueView: LabAddToQueueView,
    private val managedMessageService: ManagedMessageService,
    private val labHandler: LabHandler
) : CallbackHandler, ICallbackUtils by callbackUtils {

    override fun handleCallback(context: CallbackContext) {
        when (val data = context.data) {
            is CallbackData.LabAddToQueueAsk -> handleAsk(context)
            is CallbackData.LabAddToQueueAttempt -> handleAttempt(context, data.attempt)
            is CallbackData.LabAddToQueueCancel -> handleCancel(context)
            else -> {}
        }
    }

    private fun handleCancel(context: CallbackContext) {
        val targetUser = getUserOrDelete(context) ?: return
        val managedMessage = context.managedMessage
        val seconds = Duration.between(managedMessage.createdAt, Instant.now()).toSeconds()
        if (context.user != targetUser && seconds < SECONDS_TILL_REMOVE) {
            context.answer("Это сообщение можно будет удалить через ${SECONDS_TILL_REMOVE - seconds} секунд.")
        } else {
            context.answer("Сообщение удалено.")
            context.deleteMessage()
        }
    }

    private fun handleAsk(context: CallbackContext) {
        val lab = callbackUtils.getLabOrDelete(context) ?: return
        if (queueService.hasActiveEntry(context.user, lab)) {
            context.answer("Вы уже присутствуете в очереди")
        } else {
            val group = getGroupOrDelete(context) ?: return
            if (!group.settings!!.attemptsEnabled) {
                queueService.addToQueue(context.user, lab, 1)
                context.answer("Вы добавлены в очередь")
                labHandler.updateLabDetails(lab, context.managedMessage)
            } else {
                val directly = group.settings!!.askAttemptsDirectly
                val text = labAddToQueueView.getLabAttemptText(context.user, context.isPrivate || directly)
                val keyboard = labAddToQueueView.getLabAttemptKeyboard()
                val sendAttemptMessage = if (directly) {
                    SendMessage.builder()
                        .toUser(context.user)
                        .markdown()
                        .withTextAndInlineKeyboard(text, keyboard)
                } else {
                    context.send(group.settings!!.mainThreadId)
                        .markdown()
                        .withTextAndInlineKeyboard(text, keyboard)
                }

                managedMessageService.register(
                    sentMessage = telegram.execute(sendAttemptMessage),
                    type = MessageType.LAB_ADD_TO_QUEUE,
                    metadata = mergeMetadata(
                        labMetadata(lab),
                        userMetadata(context.user),
                        parentMessageMetadata(context.managedMessage)
                    )
                )
            }
        }
    }

    private fun handleAttempt(context: CallbackContext, attempt: Int) {
        val lab = getLabOrDelete(context) ?: return
        val targetUser = getUserOrDelete(context) ?: return

        if (context.user != targetUser) {
            context.answer("Вы не можете выбрать за другого человека")
            return
        }

        if (queueService.hasActiveEntry(context.user, lab)) {
            context.answer("Вы уже присутствуете в очереди")
        } else {
            queueService.addToQueue(context.user, lab, attempt)
            context.answer("Вы добавлены в очередь")
            val parentMessage = getParentMessage(context.managedMessage.metadata)
            if (parentMessage?.messageType == MessageType.LAB_DETAILS) labHandler.updateLabDetails(lab, parentMessage)
        }
        context.deleteMessage()
    }

    override fun prefix() = "lab_add_to_queue"

    override fun scope() = Scope.ANY

    companion object {
        private const val SECONDS_TILL_REMOVE = 60
    }
}