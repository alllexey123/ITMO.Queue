package dev.alllexey.itmoqueue.bot.command

import dev.alllexey.itmoqueue.bot.MessageContext
import dev.alllexey.itmoqueue.bot.Scope
import dev.alllexey.itmoqueue.bot.state.StateManager
import dev.alllexey.itmoqueue.services.Telegram
import org.springframework.stereotype.Component

@Component
class CancelCommand(private val stateManager: StateManager,
                    private val telegram: Telegram
) : CommandHandler {

    override fun handleMessage(context: MessageContext) {
        val handler = stateManager.removeHandler(context.chatId)
        val sendMessage = context.sendReply()
            .text(if (handler == null) "Никакое действие не требовалось" else "Действие отменено")
            .build()

        telegram.execute(sendMessage)
    }

    override fun command() = Command.CANCEL

    override fun scope() = Scope.ANY
}