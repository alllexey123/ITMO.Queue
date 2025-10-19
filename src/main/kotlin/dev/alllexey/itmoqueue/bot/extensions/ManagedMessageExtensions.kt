package dev.alllexey.itmoqueue.bot.extensions

import dev.alllexey.itmoqueue.model.ManagedMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText

fun ManagedMessage.edit(): EditMessageText.EditMessageTextBuilder<*, *> {
    return EditMessageText.builder().chatId(id.chatId).messageId(id.messageId)
}

fun ManagedMessage.delete(): DeleteMessage {
    return DeleteMessage.builder().chatId(id.chatId).messageId(id.messageId).build()
}