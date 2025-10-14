package me.alllexey123.itmoqueue.bot.extensions

import me.alllexey123.itmoqueue.model.ManagedMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText

fun ManagedMessage.edit(): EditMessageText.EditMessageTextBuilder<*, *> {
    return EditMessageText.builder().chatId(id.chatId).messageId(id.messageId)
}

fun ManagedMessage.delete(): DeleteMessage {
    return DeleteMessage.builder().chatId(id.chatId).messageId(id.messageId).build()
}