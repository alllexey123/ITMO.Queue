package me.alllexey123.itmoqueue.bot.callback

import jakarta.annotation.PostConstruct
import jakarta.transaction.Transactional
import me.alllexey123.itmoqueue.bot.command.ListSubjectsCommand
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.CallbackQuery

@Component
class CallbackManager(private val listSubjectsCommand: ListSubjectsCommand) {

    lateinit var handlers: List<CallbackHandler>

    @PostConstruct
    fun init() {
        handlers = listOf(
            listSubjectsCommand
        )
    }

    @Transactional
    fun handleCallback(callbackQuery: CallbackQuery) {
        for (handler in handlers) {
            if (callbackQuery.data.startsWith(handler.prefix())) {
                handler.handle(callbackQuery)
            }
        }
    }
}