package dev.alllexey.itmoqueue.bot.state

import jakarta.transaction.Transactional
import dev.alllexey.itmoqueue.bot.MessageContext
import dev.alllexey.itmoqueue.services.ContextService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.message.Message

@Component
class StateManager(private val contextService: ContextService) {

    val runtimeHandlers = mutableMapOf<Long, StateHandler>()

    fun setHandler(chatId: Long, handler: StateHandler) {
        runtimeHandlers[chatId] = handler
    }

    fun hasHandler(chatId: Long): Boolean {
        return runtimeHandlers.containsKey(chatId)
    }

    fun removeHandler(chatId: Long): StateHandler? {
        val handler = runtimeHandlers.remove(chatId)
        handler?.removeChatData(chatId)
        return handler
    }

    @Transactional
    fun handle(message: Message) {
        val chatId = message.chat.id

        val membership = contextService.getMembership(message)
        val user = membership?.user
            ?: contextService.getUser(message)

        val context = MessageContext(
            message = message,
            user = user,
            membership = membership,
        )

        val success = runtimeHandlers[chatId]?.handle(context) ?: true
        if (success) {
            removeHandler(chatId)
        }
    }
}