package me.alllexey123.itmoqueue.bot.command

import me.alllexey123.itmoqueue.bot.MessageContext
import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.bot.extensions.withForceReply
import me.alllexey123.itmoqueue.bot.state.EnterNicknameStateHandler
import me.alllexey123.itmoqueue.bot.state.StateManager
import me.alllexey123.itmoqueue.services.Telegram
import org.springframework.stereotype.Component

@Component
class UserSetNicknameCommand(
    private val telegram: Telegram,
    private val stateManager: StateManager,
    private val enterNicknameState: EnterNicknameStateHandler
) : CommandHandler {
    override fun handleMessage(context: MessageContext) {
        val messageBuilder = context.sendReply()
            .withForceReply("Введите новый никнейм (отмена - /cancel):")

        stateManager.setHandler(context.chatId, enterNicknameState)
        telegram.execute(messageBuilder)
    }

    override fun command() = Command.NAME

    override fun scope() = Scope.USER
}