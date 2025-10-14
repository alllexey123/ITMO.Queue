package me.alllexey123.itmoqueue.bot.view

import me.alllexey123.itmoqueue.bot.callback.CallbackData
import me.alllexey123.itmoqueue.bot.extensions.inlineButton
import me.alllexey123.itmoqueue.bot.extensions.row
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