package me.alllexey123.itmoqueue.bot.callback

import jakarta.annotation.PostConstruct
import jakarta.transaction.Transactional
import me.alllexey123.itmoqueue.bot.command.ListLabsCommand
import me.alllexey123.itmoqueue.bot.command.ListSubjectsCommand
import me.alllexey123.itmoqueue.bot.command.NewLabCommand
import me.alllexey123.itmoqueue.bot.state.StateManager
import me.alllexey123.itmoqueue.services.ContextService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.CallbackQuery

@Component
class CallbackManager(
    private val listSubjectsCommand: ListSubjectsCommand,
    private val newLabCommand: NewLabCommand,
    private val listLabsCommand: ListLabsCommand,
    private val stateManager: StateManager,
    private val contextService: ContextService
) {

    lateinit var handlers: List<CallbackHandler>

    @PostConstruct
    fun init() {
        handlers = listOf(
            listSubjectsCommand,
            newLabCommand,
            listLabsCommand
        )
    }

    @Transactional
    fun handleCallback(callbackQuery: CallbackQuery) {
        stateManager.removeHandler(callbackQuery.message.chatId)
        for (handler in handlers) {
            if (callbackQuery.data.startsWith(handler.prefix())) {
                val membership = contextService.getMembership(callbackQuery)
                val user = membership?.user
                    ?: contextService.getUser(
                        callbackQuery.from.id,
                        callbackQuery.from.userName
                    )

                val context = CallbackContext(
                    query = callbackQuery,
                    membership = membership,
                    data = handler.decode(callbackQuery.data),
                    user = user,
                    group = membership?.group,
                )
                handler.handle(context)
            }
        }
    }
}