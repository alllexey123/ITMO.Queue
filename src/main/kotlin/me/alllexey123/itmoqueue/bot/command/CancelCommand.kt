package me.alllexey123.itmoqueue.bot.command

import me.alllexey123.itmoqueue.bot.MessageContext
import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.bot.state.StateManager
import me.alllexey123.itmoqueue.services.Telegram
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

    override fun command() = "cancel"

    override fun scope() = Scope.ANY
}