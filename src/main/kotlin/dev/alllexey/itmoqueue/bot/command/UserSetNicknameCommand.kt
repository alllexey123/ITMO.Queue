package dev.alllexey.itmoqueue.bot.command

import dev.alllexey.itmoqueue.bot.MessageContext
import dev.alllexey.itmoqueue.bot.Scope
import dev.alllexey.itmoqueue.bot.extensions.withForceReply
import dev.alllexey.itmoqueue.bot.state.EnterNicknameStateHandler
import dev.alllexey.itmoqueue.bot.state.StateManager
import dev.alllexey.itmoqueue.services.Telegram
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