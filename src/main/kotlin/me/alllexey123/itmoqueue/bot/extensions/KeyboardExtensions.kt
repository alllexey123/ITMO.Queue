package me.alllexey123.itmoqueue.bot.extensions

import me.alllexey123.itmoqueue.bot.callback.CallbackData
import me.alllexey123.itmoqueue.bot.callback.CallbackDataSerializer
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow

fun inlineRowButton(text: String, callback: String?): InlineKeyboardRow =
    InlineKeyboardRow(inlineButton(text, callback))

fun inlineButton(text: String, callback: String?): InlineKeyboardButton =
    InlineKeyboardButton.builder()
        .text(text)
        .callbackData(callback)
        .build()

fun inlineRowButton(text: String, callbackData: CallbackData?): InlineKeyboardRow =
    InlineKeyboardRow(inlineButton(text, callbackData))

fun inlineButton(text: String, callbackData: CallbackData?): InlineKeyboardButton =
    inlineButton(text, callbackData?.let { CallbackDataSerializer.serialize(it) })

fun row(vararg buttons: InlineKeyboardButton): InlineKeyboardRow {
    return InlineKeyboardRow(buttons.toList())
}

fun row(buttons: List<InlineKeyboardButton>): InlineKeyboardRow {
    return InlineKeyboardRow(buttons)
}

fun InlineKeyboardMarkup.InlineKeyboardMarkupBuilder<*, *>.keyboard(vararg rows: InlineKeyboardRow): InlineKeyboardMarkup.InlineKeyboardMarkupBuilder<*, *> =
    this.keyboard(rows.toList())
