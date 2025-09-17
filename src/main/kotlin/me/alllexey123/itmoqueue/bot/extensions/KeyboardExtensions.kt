package me.alllexey123.itmoqueue.bot.extensions

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow

fun inlineRowButton(text: String, callback: String?): InlineKeyboardRow =
    InlineKeyboardRow(inlineButton(text, callback))

fun inlineButton(text: String, callback: String?): InlineKeyboardButton =
    InlineKeyboardButton.builder()
        .text(text)
        .callbackData(callback)
        .build()
