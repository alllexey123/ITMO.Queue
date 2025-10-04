package me.alllexey123.itmoqueue.bot.command

import me.alllexey123.itmoqueue.bot.MessageContext
import me.alllexey123.itmoqueue.bot.Scope
import org.springframework.stereotype.Component

@Component
class StartCommand : CommandHandler {

    override fun handleMessage(context: MessageContext) {

    }

    override fun command() = "start"

    override fun scope() = Scope.ANY
}