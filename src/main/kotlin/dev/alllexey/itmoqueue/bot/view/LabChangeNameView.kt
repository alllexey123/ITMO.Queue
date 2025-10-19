package dev.alllexey.itmoqueue.bot.view

import dev.alllexey.itmoqueue.bot.Emoji
import dev.alllexey.itmoqueue.bot.callback.CallbackData
import dev.alllexey.itmoqueue.bot.extensions.inlineButton
import dev.alllexey.itmoqueue.bot.extensions.row
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup


@Component
class LabChangeNameView {

    fun getLabChangeNameText(): String {
        return "Введите новое название лабы"
    }

    fun getLabChangeNameKeyboard(): InlineKeyboardMarkup {
        return InlineKeyboardMarkup(listOf(row(back)))
    }

    companion object Buttons {
        val back = inlineButton(Emoji.BACK, CallbackData.LabEditMenu())
    }
}