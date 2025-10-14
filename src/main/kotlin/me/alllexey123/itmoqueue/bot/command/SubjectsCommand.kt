package me.alllexey123.itmoqueue.bot.command

import me.alllexey123.itmoqueue.bot.MessageContext
import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.bot.callback.SubjectListHandler
import org.springframework.stereotype.Component

@Component
class SubjectsCommand(private val subjectListHandler: SubjectListHandler) : CommandHandler {
    override fun handleMessage(context: MessageContext) {
        subjectListHandler.sendGroupList(context.group!!, threadId = context.message.getMessageThreadId())
    }

    override fun command() = Command.SUBJECTS

    override fun scope() = Scope.GROUP
}