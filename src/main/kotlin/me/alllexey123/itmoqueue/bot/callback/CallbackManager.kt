package me.alllexey123.itmoqueue.bot.callback

import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.CallbackQuery

@Component
class CallbackManager {

    lateinit var handlers: List<CallbackHandler>

    @PostConstruct
    fun init() {
        handlers = listOf(
        )
    }

    fun handleCallback(callbackQuery: CallbackQuery) {
        for (handler in handlers) {
            if (callbackQuery.data.startsWith(handler.prefix())) {
                handler.handle(callbackQuery)
            }
        }
    }
}