package dev.alllexey.itmoqueue.bot.view

import dev.alllexey.itmoqueue.bot.callback.CallbackData
import dev.alllexey.itmoqueue.bot.extensions.inlineButton
import dev.alllexey.itmoqueue.bot.extensions.row
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup

@Component
class SubjectDeleteView {

    fun getSubjectDeleteText(): String {
        return "Вы *точно* хотите удалить этот предмет? Лабы и очереди также будут безвозвратно удалены."
    }

    fun getSubjectDeleteKeyboard(): InlineKeyboardMarkup {
        return InlineKeyboardMarkup(listOf(row(yes, no)))
    }

    companion object Buttons {
        val yes = inlineButton("Да", CallbackData.SubjectDeleteConfirm())
        val no = inlineButton("Нет", CallbackData.SubjectDeleteCancel())
    }
}