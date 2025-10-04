package me.alllexey123.itmoqueue.services

import me.alllexey123.itmoqueue.model.ManagedMessage
import me.alllexey123.itmoqueue.model.ManagedMessageId
import me.alllexey123.itmoqueue.model.enums.MessageType
import me.alllexey123.itmoqueue.repositories.ManagedMessageRepository
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.message.Message

@Service
class ManagedMessageService(
    private val repository: ManagedMessageRepository
) {

    fun register(
        sentMessage: Message,
        type: MessageType,
        metadata: MutableMap<String, Any> = mutableMapOf()
    ): ManagedMessage {
        val managedMessage = ManagedMessage(
            id = ManagedMessageId(sentMessage.chatId, sentMessage.messageId),
            messageType = type,
            metadata = metadata
        )
        return repository.save(managedMessage)
    }

    fun findById(chatId: Long, messageId: Int): ManagedMessage? {
        return repository.findById(ManagedMessageId(chatId, messageId)).orElse(null)
    }

    fun unregister(chatId: Long, messageId: Int) {
        repository.deleteById(ManagedMessageId(chatId, messageId))
    }

    fun updateMetadata(chatId: Long, messageId: Int, newMetadata: MutableMap<String, Any>) {
        val message = findById(chatId, messageId) ?: return
        message.metadata = newMetadata
        repository.save(message)
    }

    fun touch(chatId: Long, messageId: Int) {
        repository.touch(ManagedMessageId(chatId, messageId))
    }

    fun touch(managedMessage: ManagedMessage) {
        repository.touch(managedMessage.id)
    }
}