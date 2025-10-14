package me.alllexey123.itmoqueue.bot.view

import me.alllexey123.itmoqueue.bot.Emoji
import me.alllexey123.itmoqueue.bot.callback.CallbackData
import me.alllexey123.itmoqueue.bot.extensions.inlineButton
import me.alllexey123.itmoqueue.bot.extensions.row
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup


@Component
class SubjectChangeNameView {

    fun getSubjectChangeNameText(): String {
        return "Введите новое название предмета"
    }

    fun getSubjectChangeNameKeyboard(): InlineKeyboardMarkup {
        return InlineKeyboardMarkup(listOf(row(back)))
    }

    companion object Buttons {
        val back = inlineButton(Emoji.BACK, CallbackData.SubjectEditMenu())
    }
}