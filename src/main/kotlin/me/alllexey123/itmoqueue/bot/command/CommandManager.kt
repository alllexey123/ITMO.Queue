package me.alllexey123.itmoqueue.bot.command

import jakarta.transaction.Transactional
import me.alllexey123.itmoqueue.bot.MessageContext
import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.bot.extensions.withReplyTo
import me.alllexey123.itmoqueue.services.ContextService
import me.alllexey123.itmoqueue.services.Telegram
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.generics.TelegramClient

@Component
class CommandManager(
    private val telegram: Telegram,
    private val contextService: ContextService,
    private val handlers: List<CommandHandler>
) {

    @Transactional
    fun handleCommand(message: Message) {
        val text = message.text
        val command = text.split("@", limit = 2)[0].split(" ", limit = 2)[0]
        val commandNameMatches = handlers.filter { it.command().raw.equals(command, ignoreCase = true) }
        val currentScope = if (message.isUserMessage) Scope.USER else Scope.GROUP
        val fullMatches = commandNameMatches.filter { it.scope() == Scope.ANY || it.scope() == currentScope }
        if (fullMatches.isEmpty() && commandNameMatches.isNotEmpty()) {
            if (currentScope == Scope.USER) groupChatOnlyError(telegram, message)
            if (currentScope == Scope.GROUP) userChatOnlyError(telegram, message)
        } else {
            val membership = contextService.getMembership(message)
            val user = membership?.user
                ?: contextService.getUser(message)

            val context = MessageContext(
                message = message,
                user = user,
                membership = membership,
            )
            fullMatches.forEach {
                it.handleMessage(context)
            }
        }
    }

    fun groupChatOnlyError(client: TelegramClient, message: Message) {
        client.execute(
            SendMessage.builder()
                .text("Эта команда доступна только для групп")
                .withReplyTo(message)
        )
    }

    fun userChatOnlyError(client: TelegramClient, message: Message) {
        client.execute(
            SendMessage.builder()
                .text("Эта команда доступна только для ЛС")
                .withReplyTo(message)
        )
    }
}