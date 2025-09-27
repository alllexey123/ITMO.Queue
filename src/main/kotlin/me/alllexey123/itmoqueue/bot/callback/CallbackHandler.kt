package me.alllexey123.itmoqueue.bot.callback

import me.alllexey123.itmoqueue.bot.Encoder

interface CallbackHandler : Encoder {

    fun handle(context: CallbackContext)

    fun prefix(): String

    fun removePrefix(raw: String): String {
        return raw.substring(prefix().length + 1)
    }

    fun addPrefix(data: String): String {
        return prefix() + ":" + data
    }

    override fun encode(vararg objects: Any?): String {
        return addPrefix(super.encode(*objects))
    }

    override fun decode(encoded: String): List<String> {
        return super.decode(removePrefix(encoded))
    }
}