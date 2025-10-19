package dev.alllexey.itmoqueue.bot.view

import dev.alllexey.itmoqueue.bot.Emoji
import dev.alllexey.itmoqueue.bot.callback.CallbackData
import dev.alllexey.itmoqueue.bot.extensions.inlineButton
import dev.alllexey.itmoqueue.bot.extensions.row
import dev.alllexey.itmoqueue.model.Lab
import dev.alllexey.itmoqueue.model.QueueEntry
import dev.alllexey.itmoqueue.services.LabService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import java.time.format.DateTimeFormatter

@Component
class LabView(private val labService: LabService) {
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    fun getLabText(lab: Lab, entries: List<QueueEntry>, hideAttempts: Boolean = false): String {
        return buildString {
            appendLine("Лаба *${lab.name}*")
            appendLine("Предмет: *${lab.subject.name}*")
            appendLine(Emoji.DIVIDER)

            if (entries.isEmpty()) {
                appendLine("Очередь пуста")
            } else {
                entries.forEachIndexed { i, entry ->
                    val pos = String.format("%2s", i + 1)
                    val user = entry.user
                    val status = if (entry.done) Emoji.CHECK else Emoji.CANCEL
                    append("`$pos. $status` ${user.mention()}")
                    appendLine(if (!hideAttempts) " `[${entry.attemptNumber}]`" else "")
                }
            }
            appendLine(Emoji.DIVIDER)
            appendLine("Обновлено: ${timeFormatter.format(ViewUtils.timeAtStPetersburg())}")
            appendLine("[Ссылка](${labService.getLabUrl(lab)}) на полную очередь")
        }
    }

    fun getLabKeyboard(
        hideSettings: Boolean = false,
        hideNavigation: Boolean = false,
        customBackCallback: CallbackData? = null
    ): InlineKeyboardMarkup {
        val rows = mutableListOf(row(markDone, removeFromQueue, refresh))

        val back = customBackCallback?.let { inlineButton(Emoji.BACK, it) } ?: showLabList

        if (hideSettings) {
            rows += if (hideNavigation) {
                row(addToQueue)
            } else {
                row(back, addToQueue)
            }
        } else {
            rows += row(pin, edit, deleteAsk)
            rows += if (hideNavigation) {
                row(addToQueue)
            } else {
                row(back, addToQueue)
            }
        }

        return InlineKeyboardMarkup(rows)
    }

    companion object Buttons {
        val refresh = inlineButton(Emoji.REFRESH, CallbackData.LabDetailsRefresh())
        val addToQueue = inlineButton(Emoji.PLUS, CallbackData.LabAddToQueueAsk())
        val removeFromQueue = inlineButton(Emoji.MINUS, CallbackData.LabRemoveFromQueue())
        val markDone = inlineButton(Emoji.CHECK, CallbackData.LabMarkEntryDone())
        val pin = inlineButton(Emoji.PIN, CallbackData.LabPin())
        val deleteAsk = inlineButton(Emoji.DELETE, CallbackData.LabDeleteAsk())
        val edit = inlineButton(Emoji.GEAR, CallbackData.LabEditMenu())
        val showLabList = inlineButton(Emoji.BACK, CallbackData.ShowLabList())
    }
}