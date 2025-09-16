package me.alllexey123.itmoqueue.bot.command

import me.alllexey123.itmoqueue.bot.Scope
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.message.Message

@Component
class StartCommand : CommandHandler {

    override fun handle(message: Message) {

    }

    override fun command() = "start"

    override fun scope() = Scope.ANY
}