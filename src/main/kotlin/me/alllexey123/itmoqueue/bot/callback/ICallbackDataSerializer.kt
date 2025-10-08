package me.alllexey123.itmoqueue.bot.callback

interface ICallbackDataSerializer {

    fun serialize(data: CallbackData): String

    fun deserialize(rawData: String): CallbackData
}