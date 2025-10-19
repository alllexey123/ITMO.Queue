package dev.alllexey.itmoqueue.bot.view

import dev.alllexey.itmoqueue.bot.callback.CallbackData
import dev.alllexey.itmoqueue.bot.extensions.inlineButton
import dev.alllexey.itmoqueue.bot.extensions.row
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup

@Component
class NewView {

    fun getMenuText(): String {
        return "Что вы хотите создать?"
    }

    fun getMenuKeyboard(): InlineKeyboardMarkup {
        return InlineKeyboardMarkup(listOf(row(subject, lab)))
    }

    companion object Buttons {
        val subject = inlineButton("Предмет", CallbackData.NewSubjectMenu())
        val lab = inlineButton("Лаба", CallbackData.NewLabSelectSubjectPage(0))
    }
}