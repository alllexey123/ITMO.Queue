package me.alllexey123.itmoqueue.bot.view

import me.alllexey123.itmoqueue.bot.callback.CallbackData
import me.alllexey123.itmoqueue.bot.extensions.inlineButton
import me.alllexey123.itmoqueue.bot.extensions.row
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup

@Component
class LabDeleteView {

    fun getLabDeleteText(): String {
        return "Вы *точно* хотите удалить эту лабу? Очередь также будет безвозвратно удалена."
    }

    fun getLabDeleteKeyboard(): InlineKeyboardMarkup {
        return InlineKeyboardMarkup(listOf(row(yes, no)))
    }

    companion object Buttons {
        val yes = inlineButton("Да", CallbackData.LabDeleteConfirm())
        val no = inlineButton("Нет", CallbackData.LabDeleteCancel())
    }
}