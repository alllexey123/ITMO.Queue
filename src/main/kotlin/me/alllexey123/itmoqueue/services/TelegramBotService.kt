package me.alllexey123.itmoqueue.services

import jakarta.annotation.PostConstruct
import me.alllexey123.itmoqueue.bot.BotProperties
import me.alllexey123.itmoqueue.bot.TelegramBot
import org.springframework.stereotype.Service
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication

@Service
class TelegramBotService(private val botProperties: BotProperties, private val telegramBot: TelegramBot) {

    lateinit var app: TelegramBotsLongPollingApplication

    @PostConstruct
    fun init() {
        app = TelegramBotsLongPollingApplication()
        app.registerBot(botProperties.token, telegramBot)
    }
}