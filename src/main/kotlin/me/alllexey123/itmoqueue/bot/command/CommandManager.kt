package me.alllexey123.itmoqueue.bot.command

import jakarta.annotation.PostConstruct
import jakarta.transaction.Transactional
import me.alllexey123.itmoqueue.bot.MessageContext
import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.services.ContextService
import me.alllexey123.itmoqueue.services.Telegram
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.message.Message

@Component
class CommandManager(
    private val newLabCommand: NewLabCommand,
    private val listLabsCommand: ListLabsCommand,
    private val startCommand: StartCommand,
    private val newSubjectCommand: NewSubjectCommand,
    private val cancelCommand: CancelCommand,
    private val telegram: Telegram,
    private val listSubjectsCommand: ListSubjectsCommand,
    private val contextService: ContextService
) {

    lateinit var handlers: List<CommandHandler>

    @PostConstruct
    fun init() {
        handlers = listOf(
            startCommand,
            cancelCommand,
            newSubjectCommand,
            listSubjectsCommand,
            newLabCommand,
            listLabsCommand,
        )
    }

    @Transactional
    fun handleCommand(message: Message) {
        val text = message.text
        val command = text.split("@", limit = 2)[0].split(" ", limit = 2)[0].substring(1)
        handlers.forEach { handler ->
            handler.run {
                if (command().equals(command, ignoreCase = true)) {
                    val membership = contextService.getMembership(message)
                    val user = membership?.user
                        ?: contextService.getUser(message)

                    val context = MessageContext(
                        message = message,
                        user = user,
                        membership = membership,
                    )

                    if (scope() == Scope.ANY) {
                        handle(context)
                    } else {
                        val isUserChat = message.isUserMessage
                        if (scope() == Scope.USER) {
                            if (isUserChat) handle(context) else userChatOnlyError(telegram, message)
                        }
                        if (scope() == Scope.GROUP) {
                            if (!isUserChat) handle(context) else groupChatOnlyError(telegram, message)
                        }
                    }
                }
            }
        }
    }
}