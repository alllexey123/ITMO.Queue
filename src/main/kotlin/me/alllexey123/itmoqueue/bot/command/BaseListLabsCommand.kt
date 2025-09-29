package me.alllexey123.itmoqueue.bot.command

import me.alllexey123.itmoqueue.bot.Emoji
import me.alllexey123.itmoqueue.bot.callback.CallbackContext
import me.alllexey123.itmoqueue.bot.callback.CallbackHandler
import me.alllexey123.itmoqueue.model.LabWork
import me.alllexey123.itmoqueue.services.LabWorkService
import me.alllexey123.itmoqueue.services.QueueService
import me.alllexey123.itmoqueue.services.Telegram
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import me.alllexey123.itmoqueue.bot.extensions.*
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

abstract class BaseListLabsCommand(
    private val telegram: Telegram,
    private val labWorkService: LabWorkService,
    private val queueService: QueueService
) : CommandHandler, CallbackHandler {

    protected abstract fun getLabKeyboard(lab: LabWork?, pinned: Boolean): InlineKeyboardMarkup
    protected abstract fun editLabDataMessage(chatId: Long, messageId: Int, lab: LabWork?, pinned: Boolean)
    protected abstract fun updateLabsList(context: CallbackContext)

    protected val secondsPerRefresh: Long = 3

    override fun handle(context: CallbackContext) {
        when (context.asString(0)) {
            "select" -> handleLabSelectQuery(context)
            "add_to_queue" -> handleLabAddToQueueQuery(context)
            "remove_from_queue" -> handleRemoveFromQueueQuery(context)
            "add_to_queue_attempt" -> handleAddToQueueAttemptQuery(context)
            "add_to_queue_cancel" -> handleAddToQueueCancelQuery(context)
            "mark_done" -> handleMarkDoneQuery(context)
            "main" -> updateLabsList(context)
        }
    }

    private fun handleLabSelectQuery(context: CallbackContext) {
        val lab = labWorkService.findById(context.asLong(1))
        val lastUpdate = context.data.getOrNull(2)?.let { LocalDateTime.parse(it) }
        val pinned = context.asBoolean(3)

        if (lastUpdate != null && lastUpdate.plusSeconds(secondsPerRefresh).isAfter(LocalDateTime.now())) {
            val remaining = ChronoUnit.SECONDS.between(LocalDateTime.now(), lastUpdate.plusSeconds(secondsPerRefresh))
            val answer = context.answerBuilder()
                .text("Не так быстро! Обновлять можно раз в $secondsPerRefresh секунды.")
                .cacheTime(remaining.toInt())
                .build()
            telegram.execute(answer)
        } else {
            editLabDataMessage(context.chatId, context.messageId, lab, pinned)
        }
    }

    private fun handleLabAddToQueueQuery(context: CallbackContext) {
        val labId = context.asLong(1)
        val lab = labWorkService.findById(labId) ?: return
        val queue = lab.queues.first()
        val pinned = context.asBoolean(2)

        if (queueService.isActiveInQueue(context.user, queue)) {
            val answer = context.answerBuilder()
                .text("Вы уже присутствуете в очереди!")
                .cacheTime(3)
                .build()
            telegram.execute(answer)
        } else {
            val message = context.send()
                .text("${context.user.mention()}, какая это попытка?\n\n_У вас 1 минута на выбор, потом сообщение могут удалить_")
                .parseMode(ParseMode.MARKDOWN)
                .replyMarkup(getAttemptKeyboard(lab.id, context.user.telegramId, context.messageId, pinned))
                .build()
            telegram.execute(message)
        }
    }

    private fun handleRemoveFromQueueQuery(context: CallbackContext) {
        val lab = labWorkService.findById(context.asLong(1)) ?: return
        val pinned = context.asBoolean(2)
        val answer = context.answerBuilder()

        if (queueService.removeUserFromQueue(context.user, lab.queues.first())) {
            answer.text("Вы успешно удалены из очереди")
            editLabDataMessage(context.chatId, context.messageId, lab, pinned)
        } else {
            answer.text("Вас нет в этой очереди")
        }
        telegram.execute(answer.build())
    }

    private fun handleAddToQueueAttemptQuery(context: CallbackContext) {
        val userId = context.asLong(3)
        if (context.user.telegramId != userId) {
            val answer = context.answerBuilder().text("Эта кнопочка не для вас :)").build()
            telegram.execute(answer)
            return
        }

        val lab = labWorkService.findById(context.asLong(1)) ?: return
        val attempt = context.asInt(2)
        val mainMessageId = context.asInt(4)
        val pinned = context.asBoolean(5)
        val queue = lab.queues.first()

        val answer = context.answerBuilder()
        if (queueService.isActiveInQueue(context.user, queue)) {
            answer.text("Вы уже присутствуете в очереди")
        } else {
            queueService.addToQueue(context.user, queue, attempt)
            answer.text("Вы добавлены в очередь")
            editLabDataMessage(context.chatId, mainMessageId, lab, pinned)
        }

        telegram.execute(answer.build())
        telegram.execute(DeleteMessage(context.chatId.toString(), context.messageId))
    }

    private fun handleAddToQueueCancelQuery(context: CallbackContext) {
        val askedUserId = context.asLong(1)
        val askedAt = Instant.ofEpochMilli(context.asLong(2)).atZone(ZoneId.systemDefault()).toLocalDateTime()
        val timeoutSeconds = 60

        if (LocalDateTime.now().isAfter(askedAt.plusSeconds(timeoutSeconds.toLong()))) {
            telegram.execute(context.deleteBuilder().build())
            return
        }

        val answer = context.answerBuilder()
        if (askedUserId == context.user.telegramId) {
            answer.text("Выбор отменён")
            telegram.execute(context.deleteBuilder().build())
        } else {
            answer.text("Вы не можете пока удалить это сообщение")
        }
        telegram.execute(answer.build())
    }

    private fun handleMarkDoneQuery(context: CallbackContext) {
        val queue = queueService.findQueueById(context.asLong(1))
        val entry = queue?.let { queueService.findEntryByQueueAndUser(context.user, it, false) }
        val pinned = context.asBoolean(2)

        val answer = context.answerBuilder()
        if (entry == null) {
            answer.text("У вас нет активных позиций в этой очереди")
        } else {
            entry.done = true
            answer.text("Ваша позиция отмечена как завершённая")
            editLabDataMessage(context.chatId, context.messageId, queue.labWork, pinned)
        }
        telegram.execute(answer.build())
    }

    fun getLabText(lab: LabWork?): String {
        if (lab == null) return "Лаба не найдена"

        val queue = lab.queues.first()
        val entries = queueService.sortedEntries(queue)
        val dtf = DateTimeFormatter.ofPattern("HH:mm:ss")

        return buildString {
            appendLine("Лаба *${lab.name}*")
            appendLine("Предмет: *${lab.subject.name}*")
            appendLine("Обновлено: ${dtf.format(LocalTime.now())}\n")

            if (entries.isEmpty()) {
                appendLine("Очередь пуста")
            } else {
                appendLine("Очередь:")
                entries.forEachIndexed { i, entry ->
                    val user = entry.user
                    val status = if (entry.done) Emoji.CHECK else Emoji.CANCEL
                    appendLine("`${i + 1}. [${entry.attemptNumber}|$status]` ${user.mention()}")
                }
            }
        }
    }

    private fun getAttemptKeyboard(labId: Long?, telegramUserId: Long, mainMessageId: Int, pinned: Boolean): InlineKeyboardMarkup {
        val count = 4
        val buttons = (1..count).map { i ->
            val text = if (i == count) "$i+" else "$i"
            inlineButton(text, encode("add_to_queue_attempt", labId, i, telegramUserId, mainMessageId, pinned))
        } + inlineButton(Emoji.CANCEL, encode("add_to_queue_cancel", telegramUserId, System.currentTimeMillis()))

        return InlineKeyboardMarkup(listOf(InlineKeyboardRow(buttons)))
    }
}