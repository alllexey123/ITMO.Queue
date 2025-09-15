package me.alllexey123.itmoqueue.bot

import jakarta.annotation.PostConstruct
import me.alllexey123.itmoqueue.bot.command.CommandManager
import org.springframework.stereotype.Component
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class TelegramBot(
    private val commandManager: CommandManager,
    private val myChatMemberHandler: MyChatMemberHandler
) : LongPollingSingleThreadUpdateConsumer {

    @PostConstruct
    fun init() {

    }

    override fun consume(update: Update?) {
        if (update == null) return
        if (update.hasMessage() && update.message.hasText()) {
            val message = update.message
            if (message.isCommand) {
                commandManager.onCommand(message)
            }
        }

        if (update.hasMyChatMember()) {
            myChatMemberHandler.handle(update.myChatMember)
        }
    }
}