package dev.alllexey.itmoqueue.bot.callback

import dev.alllexey.itmoqueue.bot.Scope

interface CallbackHandler {

    fun handleCallback(context: CallbackContext)

    fun prefix(): String

    fun scope(): Scope
}