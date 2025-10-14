package me.alllexey123.itmoqueue.services

import jakarta.annotation.PostConstruct
import me.alllexey123.itmoqueue.model.ManagedMessage
import me.alllexey123.itmoqueue.model.ManagedMessageId
import me.alllexey123.itmoqueue.model.enums.MessageType
import me.alllexey123.itmoqueue.repositories.ManagedMessageRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.objects.message.Message

@Service
class ManagedMessageService(
    private val repository: ManagedMessageRepository,
    private val telegram: Telegram
) {

    @PostConstruct
    @Transactional
    fun init() {
        repository.findAll().forEach {
            try {
                val delete = DeleteMessage.builder()
                    .chatId(it.id.chatId)
                    .messageId(it.id.messageId)
                    .build()
                telegram.execute(delete)
            } catch (_: Exception) {

            }
        }
    }

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

    fun findById(managedMessageId: ManagedMessageId?): ManagedMessage? {
        if (managedMessageId == null) return null
        return repository.findByIdOrNull(managedMessageId)
    }

    fun findById(chatId: Long?, messageId: Int?): ManagedMessage? {
        if (chatId == null || messageId == null) return null
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