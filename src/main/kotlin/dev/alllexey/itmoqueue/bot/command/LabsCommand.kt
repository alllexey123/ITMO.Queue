package dev.alllexey.itmoqueue.bot.command

import dev.alllexey.itmoqueue.bot.MessageContext
import dev.alllexey.itmoqueue.bot.Scope
import dev.alllexey.itmoqueue.bot.callback.LabListHandler
import org.springframework.stereotype.Component

@Component
class LabsCommand(private val labListHandler: LabListHandler) : CommandHandler {

    override fun handleMessage(context: MessageContext) {
        if (context.isPrivate) {
            labListHandler.sendUserList(context.user)
        } else {
            labListHandler.sendGroupList(context.group!!, threadId = context.message.getMessageThreadId())
        }
    }

    override fun command() = Command.LABS

    override fun scope() = Scope.ANY
}