package me.alllexey123.itmoqueue.bot.callback

abstract class ExtendedCallbackHandler(
    protected val handlers: List<SubCallbackHandler>
) : CallbackHandler {

    override fun handle(context: CallbackContext) {

    }

    abstract override fun prefix(): String
}