package me.alllexey123.itmoqueue.bot.command

import me.alllexey123.itmoqueue.bot.MessageContext
import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.bot.callback.NewHandler
import me.alllexey123.itmoqueue.services.Telegram
import org.springframework.stereotype.Component

@Component
class NewCommand(
    private val telegram: Telegram,
    private val newHandler: NewHandler
) : CommandHandler {

    override fun handleMessage(context: MessageContext) {
        if (!context.requireAdmin(telegram)) return
        newHandler.sendMenu(context.group!!)
    }

    override fun command() = Command.NEW

    override fun scope() = Scope.GROUP
}