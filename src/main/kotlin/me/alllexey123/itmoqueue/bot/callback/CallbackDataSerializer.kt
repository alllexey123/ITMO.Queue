package me.alllexey123.itmoqueue.bot.callback

object CallbackDataSerializer {

    fun serialize(data: CallbackData): String {
        val parts = mutableListOf(data.handlerPrefix, data.actionPrefix)
        parts.addAll(data.toPayload())
        return parts.joinToString(":")
    }

    fun deserialize(rawData: String): CallbackData {
        val parts = rawData.split(":")
        val key = parts.take(2).joinToString(":")
        val payload = parts.drop(2)

        val creator = CallbackData.findCreator(key)
            ?: throw IllegalArgumentException("Unknown callback data prefix: $key")

        return try {
            creator(payload)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to create CallbackData for key '$key' with payload '$payload'", e)
        }
    }
}