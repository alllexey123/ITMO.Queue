package me.alllexey123.itmoqueue.bot.callback

class CallbackData(
    val raw: List<String>,
    var currentIndex: Int = 0
) : ICallbackData {
    override fun resetIndex() {
        currentIndex = 0
    }

    override fun asString(index: Int): String {
        return raw[index]
    }

    override fun asLong(index: Int): Long {
        return raw[index].toLong()
    }

    override fun asInt(index: Int): Int {
        return raw[index].toInt()
    }

    override fun asBoolean(index: Int): Boolean {
        return raw[index].toBoolean()
    }

    override fun nextString(): String {
        currentIndex++
        return raw[currentIndex]
    }

    override fun nextLong(): Long {
        return nextString().toLong()
    }

    override fun nextInt(): Int {
        return nextString().toInt()
    }

    override fun nextBoolean(): Boolean {
        return nextString().toBoolean()
    }

    override fun size(): Int {
        return raw.size
    }
}