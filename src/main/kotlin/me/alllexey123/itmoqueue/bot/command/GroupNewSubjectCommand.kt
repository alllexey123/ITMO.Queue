package me.alllexey123.itmoqueue.bot.command

import me.alllexey123.itmoqueue.bot.MessageContext
import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.bot.extensions.withForceReply
import me.alllexey123.itmoqueue.bot.state.EnterSubjectNameState
import me.alllexey123.itmoqueue.bot.state.StateManager
import me.alllexey123.itmoqueue.services.Telegram
import org.springframework.stereotype.Component

@Component
class GroupNewSubjectCommand(
    private val telegram: Telegram,
    private val stateManager: StateManager,
    private val enterSubjectNameState: EnterSubjectNameState
) : CommandHandler {

    override fun handleMessage(context: MessageContext) {
        if (!context.requireAdmin(telegram)) return
        val messageBuilder = context.sendReply()
            .withForceReply("Введите новое название предмета (отмена - /cancel):")

        stateManager.setHandler(context.chatId, enterSubjectNameState)
        telegram.execute(messageBuilder)
    }

    override fun command() = NAME

    override fun scope() = Scope.GROUP

    companion object {
        const val NAME = "new_subject"
    }

}