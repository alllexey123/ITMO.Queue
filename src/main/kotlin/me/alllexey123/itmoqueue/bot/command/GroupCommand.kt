package me.alllexey123.itmoqueue.bot.command

import me.alllexey123.itmoqueue.bot.MessageContext
import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.bot.callback.GroupListHandler
import me.alllexey123.itmoqueue.services.Telegram
import org.springframework.stereotype.Component

@Component
class GroupCommand(
    private val telegram: Telegram,
    private val groupListHandler: GroupListHandler
) : CommandHandler {

    override fun handleMessage(context: MessageContext) {
        if (context.isPrivate) {
            groupListHandler.sendGroupList(context.user)
        } else {
            if (!context.requireAdmin(telegram)) return
            groupListHandler.sendGroupMenu(context.group!!, threadId = context.message.getMessageThreadId())
        }
    }

    override fun command() = Command.GROUP

    override fun scope() = Scope.ANY
}