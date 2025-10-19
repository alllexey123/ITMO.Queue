package dev.alllexey.itmoqueue.bot.command

import dev.alllexey.itmoqueue.bot.MessageContext
import dev.alllexey.itmoqueue.bot.Scope

interface CommandHandler {

    fun handleMessage(context: MessageContext)

    fun command(): Command

    fun scope(): Scope
}