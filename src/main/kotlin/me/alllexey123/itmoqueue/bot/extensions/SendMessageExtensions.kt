package me.alllexey123.itmoqueue.bot.extensions

import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup

fun SendMessage.SendMessageBuilder<*, *>.withForceReply(text: String): SendMessage {
    return this.text(text)
        .replyMarkup(
            ForceReplyKeyboard.builder()
                .forceReply(true)
                .selective(true)
                .build()
        )
        .build()
}

fun SendMessage.SendMessageBuilder<*, *>.withTextAndInlineKeyboard(
    text: String,
    keyboard: InlineKeyboardMarkup
): SendMessage {
    return this.text(text).replyMarkup(keyboard).build()
}

fun SendMessage.SendMessageBuilder<*, *>.replyTo(message: Message): SendMessage.SendMessageBuilder<*, *> {
    return this.replyToMessageId(message.messageId).chatId(message.chatId)
}

fun SendMessage.SendMessageBuilder<*, *>.toChatWith(message: Message): SendMessage.SendMessageBuilder<*, *> {
    return this.chatId(message.chatId)
}

fun SendMessage.SendMessageBuilder<*, *>.toChatWith(message: MaybeInaccessibleMessage): SendMessage.SendMessageBuilder<*, *> {
    return this.chatId(message.chatId)
}


fun SendMessage.SendMessageBuilder<*, *>.withReplyTo(message: Message): SendMessage {
    return this.replyToMessageId(message.messageId).chatId(message.chatId).build()
}