package me.alllexey123.itmoqueue.bot.callback

import jakarta.annotation.PostConstruct
import jakarta.transaction.Transactional
import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.bot.command.GroupListLabsCommand
import me.alllexey123.itmoqueue.bot.command.GroupListSubjectsCommand
import me.alllexey123.itmoqueue.bot.command.GroupNewLabCommand
import me.alllexey123.itmoqueue.bot.command.UserListLabsCommand
import me.alllexey123.itmoqueue.bot.state.StateManager
import me.alllexey123.itmoqueue.services.ContextService
import me.alllexey123.itmoqueue.services.ManagedMessageService
import me.alllexey123.itmoqueue.services.Telegram
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.CallbackQuery

@Component
class CallbackManager(
    private val groupListSubjectsCommand: GroupListSubjectsCommand,
    private val groupNewLabCommand: GroupNewLabCommand,
    private val groupListLabsCommand: GroupListLabsCommand,
    private val stateManager: StateManager,
    private val contextService: ContextService,
    private val userListLabsCommand: UserListLabsCommand,
    private val callbackDataSerializer: CallbackDataSerializer,
    private val managedMessageService: ManagedMessageService,
    private val telegram: Telegram
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

            val context = CallbackContext(
                query = callbackQuery,
                membership = membership,
                data = callbackDataSerializer.deserialize(callbackQuery.data),
                managedMessage = managedMessageService.findById(callbackQuery.message.chatId, callbackQuery.message.messageId),
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