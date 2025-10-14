package me.alllexey123.itmoqueue.bot.command

import me.alllexey123.itmoqueue.bot.MessageContext
import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.bot.callback.LabListHandler
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