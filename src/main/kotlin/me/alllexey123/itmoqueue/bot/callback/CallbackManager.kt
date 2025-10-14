package me.alllexey123.itmoqueue.bot.callback

import jakarta.transaction.Transactional
import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.bot.state.StateManager
import me.alllexey123.itmoqueue.services.ContextService
import me.alllexey123.itmoqueue.services.ManagedMessageService
import me.alllexey123.itmoqueue.services.Telegram
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.objects.CallbackQuery

@Component
class CallbackManager(
    private val stateManager: StateManager,
    private val contextService: ContextService,
    private val managedMessageService: ManagedMessageService,
    private val telegram: Telegram,
    private val handlers: List<CallbackHandler>
) {

    @Transactional
    fun handleCallback(callbackQuery: CallbackQuery) {
        stateManager.removeHandler(callbackQuery.message.chatId)

        val prefixMatches = handlers.filter { callbackQuery.data.startsWith(it.prefix()) }
        val currentScope = if (callbackQuery.message.isUserMessage) Scope.USER else Scope.GROUP
        val fullMatches = prefixMatches.filter { it.scope() == Scope.ANY || it.scope() == currentScope }
        if (fullMatches.isEmpty() && prefixMatches.isNotEmpty()) {
            // do smth?
        } else {
            val membership = contextService.getMembership(callbackQuery)
            val user = membership?.user
                ?: contextService.getUser(
                    callbackQuery.from.id,
                    callbackQuery.from.userName
                )

            val managedMessage = managedMessageService.findById(callbackQuery.message.chatId, callbackQuery.message.messageId)
            if (managedMessage == null) {
                val delete = DeleteMessage.builder()
                    .chatId(callbackQuery.message.chatId)
                    .messageId(callbackQuery.message.messageId)
                    .build()
                telegram.execute(delete)
                return
            }

            val context = CallbackContext(
                query = callbackQuery,
                membership = membership,
                data = CallbackDataSerializer.deserialize(callbackQuery.data),
                managedMessage = managedMessage,
                user = user,
                group = membership?.group,
                telegram = telegram,
                managedMessageService = managedMessageService
            )
            fullMatches.forEach {
                it.handleCallback(context)
            }
        }
    }
}