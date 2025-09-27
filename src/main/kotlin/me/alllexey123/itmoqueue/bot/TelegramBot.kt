package me.alllexey123.itmoqueue.bot

import jakarta.annotation.PostConstruct
import me.alllexey123.itmoqueue.bot.callback.CallbackManager
import me.alllexey123.itmoqueue.bot.command.CommandManager
import me.alllexey123.itmoqueue.bot.state.StateManager
import org.springframework.stereotype.Component
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class TelegramBot(
    private val commandManager: CommandManager,
    private val myChatMemberHandler: MyChatMemberHandler,
    private val callbackManager: CallbackManager,
    private val stateManager: StateManager
) : LongPollingSingleThreadUpdateConsumer {

    @PostConstruct
    fun init() {

    }

    override fun consume(update: Update?) {
        if (update == null) return

        if (update.hasMessage() && update.message.hasText()) {
            val message = update.message
            if (message.isCommand) {
                commandManager.handleCommand(message)
            } else {
                stateManager.handle(message)
            }
        }

        if (update.hasMyChatMember()) {
            myChatMemberHandler.handle(update.myChatMember)
        }

        if (update.hasCallbackQuery()) {
            callbackManager.handleCallback(update.callbackQuery)
        }
    }
}