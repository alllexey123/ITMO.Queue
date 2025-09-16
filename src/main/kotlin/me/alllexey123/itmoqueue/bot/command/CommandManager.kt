package me.alllexey123.itmoqueue.bot.command

import jakarta.annotation.PostConstruct
import jakarta.transaction.Transactional
import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.services.TelegramService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.message.Message

@Component
class CommandManager(
    private val newLabCommand: NewLabCommand,
    private val listLabsCommand: ListLabsCommand,
    private val startCommand: StartCommand,
    private val newSubjectCommand: NewSubjectCommand,
    private val cancelCommand: CancelCommand,
    private val telegramService: TelegramService,
    private val listSubjectsCommand: ListSubjectsCommand
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
                    if (scope() == Scope.ANY) {
                        handle(message)
                    } else {
                        val isUserChat = message.chat.isUserChat
                        if (scope() == Scope.USER) {
                            if (isUserChat) handle(message) else groupChatOnlyError(telegramService.client, message)
                        }
                        if (scope() == Scope.GROUP) {
                            if (!isUserChat) handle(message) else userChatOnlyError(telegramService.client, message)
                        }
                    }
                }
            }
        }
    }
}