package dev.alllexey.itmoqueue.bot.command

import dev.alllexey.itmoqueue.bot.MessageContext
import dev.alllexey.itmoqueue.bot.Scope
import dev.alllexey.itmoqueue.bot.callback.SubjectListHandler
import org.springframework.stereotype.Component

@Component
class SubjectsCommand(private val subjectListHandler: SubjectListHandler) : CommandHandler {
    override fun handleMessage(context: MessageContext) {
        subjectListHandler.sendGroupList(context.group!!, threadId = context.message.getMessageThreadId())
    }

    override fun command() = Command.SUBJECTS

    override fun scope() = Scope.GROUP
}