package me.alllexey123.itmoqueue.bot.callback

import me.alllexey123.itmoqueue.bot.Scope

interface CallbackHandler {

    fun handleCallback(context: CallbackContext)

    fun prefix(): String

    fun scope(): Scope
}