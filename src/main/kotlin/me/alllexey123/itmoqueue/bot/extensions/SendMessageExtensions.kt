package me.alllexey123.itmoqueue.bot.extensions

import me.alllexey123.itmoqueue.model.User
import org.telegram.telegrambots.meta.api.methods.ParseMode
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

fun SendMessage.SendMessageBuilder<*, *>.toThread(messageThreadId: Int?): SendMessage.SendMessageBuilder<*, *> {
    if (messageThreadId == null) return this
    return this.messageThreadId(messageThreadId)
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

fun SendMessage.SendMessageBuilder<*, *>.toUser(user: User): SendMessage.SendMessageBuilder<*, *> {
    return this.chatId(user.telegramId)
}

fun SendMessage.SendMessageBuilder<*, *>.markdown(): SendMessage.SendMessageBuilder<*, *> {
    return this.parseMode(ParseMode.MARKDOWN)
}


fun SendMessage.SendMessageBuilder<*, *>.withReplyTo(message: Message): SendMessage {
    return this.replyToMessageId(message.messageId).chatId(message.chatId).build()
}