package me.alllexey123.itmoqueue.bot.command

import me.alllexey123.itmoqueue.bot.MessageContext
import me.alllexey123.itmoqueue.bot.Scope

interface CommandHandler {

    fun handleMessage(context: MessageContext)

    fun command(): String

    fun scope(): Scope
}