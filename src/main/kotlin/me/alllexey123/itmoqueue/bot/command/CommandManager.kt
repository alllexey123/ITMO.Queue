package me.alllexey123.itmoqueue.bot.command

import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.message.Message

@Component
class CommandManager(
    private val newQueueCommand: NewQueueCommand,
    private val listQueuesCommand: ListQueuesCommand,
    private val startCommand: StartCommand
) {

    lateinit var handlers: List<CommandHandler>

    @PostConstruct
    fun init() {
        handlers = listOf(
            startCommand,
            newQueueCommand,
            listQueuesCommand,
        )
    }

    fun onCommand(message: Message) {
        val text = message.text
        val command = text.split("@", limit = 2)[0].split(" ", limit = 2)[0].substring(1)
        handlers.forEach { handler ->
            handler.run {
                if (command().equals(command, ignoreCase = true)) {
                    handle(message)
                }
            }
        }
    }
}