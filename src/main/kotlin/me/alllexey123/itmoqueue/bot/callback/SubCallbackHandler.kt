package me.alllexey123.itmoqueue.bot.callback

abstract class SubCallbackHandler(
    protected val parent: ExtendedCallbackHandler
) : CallbackHandler{

    abstract override fun handle(context: CallbackContext)

    abstract override fun prefix(): String

    override fun encode(vararg objects: Any?): String {
        return parent.encode(this.prefix(), objects)
    }

    override fun decode(encoded: String): List<String> {
        return parent.decode(encoded).drop(1)
    }
}