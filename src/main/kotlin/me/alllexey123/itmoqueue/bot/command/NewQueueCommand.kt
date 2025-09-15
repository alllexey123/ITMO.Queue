package me.alllexey123.itmoqueue.bot.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.message.Message

@Component
class NewQueueCommand : CommandHandler {

    override fun handle(message: Message) {
        
    }

    override fun command() = "new_queue"
}