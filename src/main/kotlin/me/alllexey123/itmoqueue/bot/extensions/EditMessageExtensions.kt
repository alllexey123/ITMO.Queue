package me.alllexey123.itmoqueue.bot.extensions

import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup

fun EditMessageText.EditMessageTextBuilder<*, *>.edit(
    message: Message
): EditMessageText.EditMessageTextBuilder<*, *> =
    this.messageId(message.messageId).chatId(message.chatId)

fun EditMessageText.EditMessageTextBuilder<*, *>.edit(
    message: MaybeInaccessibleMessage
): EditMessageText.EditMessageTextBuilder<*, *> =
    this.messageId(message.messageId).chatId(message.chatId)

fun EditMessageText.EditMessageTextBuilder<*, *>.withInlineKeyboard(
    text: String,
    keyboard: InlineKeyboardMarkup
): EditMessageText {
    return this.text(text).replyMarkup(keyboard).build()
}