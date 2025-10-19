package dev.alllexey.itmoqueue.bot.view

import dev.alllexey.itmoqueue.bot.Emoji
import dev.alllexey.itmoqueue.bot.callback.CallbackData
import dev.alllexey.itmoqueue.bot.extensions.inlineButton
import dev.alllexey.itmoqueue.bot.extensions.row
import dev.alllexey.itmoqueue.model.Subject
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup

@Component
class SubjectEditView {

    fun getSubjectEditText(subject: Subject, hideQueueType: Boolean = false): String {
        return buildString {
            appendLine("Предмет *${subject.name}*")
            if (!hideQueueType) {
                appendLine(Emoji.DIVIDER)
                appendLine("Тип очереди: *${subject.mergedQueueType.ruTitle}*")
            }
        }
    }

    fun getSubjectEditKeyboard(hideQueueTypeAsk: Boolean = false): InlineKeyboardMarkup {
        val rows = mutableListOf(row(changeName))
        if (!hideQueueTypeAsk) rows += row(changeQueueType)
        rows += row(back)
        return InlineKeyboardMarkup(rows)
    }

    companion object Buttons {
        val changeQueueType = inlineButton("Изменить тип очереди", CallbackData.SubjectQueueTypeAsk(false))
        val changeName = inlineButton(Emoji.PENCIL, CallbackData.SubjectEditChangeName())
        val back = inlineButton(Emoji.BACK, CallbackData.SubjectDetailsRefresh())
    }
}