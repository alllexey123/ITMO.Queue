package dev.alllexey.itmoqueue.bot.view

import dev.alllexey.itmoqueue.bot.Emoji
import dev.alllexey.itmoqueue.bot.callback.CallbackData
import dev.alllexey.itmoqueue.bot.extensions.inlineButton
import dev.alllexey.itmoqueue.bot.extensions.row
import dev.alllexey.itmoqueue.model.Lab
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup

@Component
class LabEditView {

    fun getLabEditText(lab: Lab, hideQueueType: Boolean = false): String {
        return buildString {
            appendLine("Лаба *${lab.name}*")
            appendLine("Предмет: *${lab.subject.name}*")
            if (!hideQueueType) {
                appendLine(Emoji.DIVIDER)
                appendLine("Тип очереди: *${lab.queueType.ruTitle}*")
            }
        }
    }

    fun getLabEditKeyboard(hideQueueTypeAsk: Boolean = false): InlineKeyboardMarkup {
        val rows = mutableListOf(row(changeName))
        if (!hideQueueTypeAsk) rows += row(changeQueueType)
        rows += row(back)
        return InlineKeyboardMarkup(rows)
    }

    companion object Buttons {
        val changeQueueType = inlineButton("Изменить тип очереди", CallbackData.LabQueueTypeAsk(false))
        val changeName = inlineButton(Emoji.PENCIL, CallbackData.LabEditChangeName())
        val back = inlineButton(Emoji.BACK, CallbackData.LabDetailsRefresh())
    }
}