package me.alllexey123.itmoqueue.bot.callback

import jakarta.annotation.PostConstruct
import jakarta.transaction.Transactional
import me.alllexey123.itmoqueue.bot.command.GroupListLabsCommand
import me.alllexey123.itmoqueue.bot.command.GroupListSubjectsCommand
import me.alllexey123.itmoqueue.bot.command.GroupNewLabCommand
import me.alllexey123.itmoqueue.bot.command.UserListLabsCommand
import me.alllexey123.itmoqueue.bot.state.StateManager
import me.alllexey123.itmoqueue.services.ContextService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.CallbackQuery

@Component
class CallbackManager(
    private val groupListSubjectsCommand: GroupListSubjectsCommand,
    private val groupNewLabCommand: GroupNewLabCommand,
    private val groupListLabsCommand: GroupListLabsCommand,
    private val stateManager: StateManager,
    private val contextService: ContextService,
    private val userListLabsCommand: UserListLabsCommand
) {

    lateinit var handlers: List<CallbackHandler>

    @PostConstruct
    fun init() {
        handlers = listOf(
            groupListSubjectsCommand,
            groupNewLabCommand,
            groupListLabsCommand,
            userListLabsCommand
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