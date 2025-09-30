package me.alllexey123.itmoqueue.bot.callback

interface ICallbackData {

    fun resetIndex()

    fun asString(index: Int): String

    fun asLong(index: Int): Long

    fun asInt(index: Int): Int

    fun asBoolean(index: Int): Boolean

    fun nextString(): String

    fun nextLong(): Long

    fun nextInt(): Int

    fun nextBoolean(): Boolean

    fun size(): Int
}