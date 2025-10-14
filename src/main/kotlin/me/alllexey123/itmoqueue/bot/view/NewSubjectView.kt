package me.alllexey123.itmoqueue.bot.view

import me.alllexey123.itmoqueue.bot.Emoji
import me.alllexey123.itmoqueue.bot.callback.CallbackData
import me.alllexey123.itmoqueue.bot.extensions.inlineButton
import me.alllexey123.itmoqueue.bot.extensions.row
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup

@Component
class NewSubjectView {

    fun getNewSubjectText(): String {
        return "Введите название для предмета (ответом на это сообщение)\n\nотмена - /cancel"
    }

    fun getNewSubjectKeyboard(): InlineKeyboardMarkup {
        return InlineKeyboardMarkup(listOf(row(back)))
    }

    companion object Buttons {
        val back = inlineButton(Emoji.BACK, CallbackData.NewMenu())
    }
}