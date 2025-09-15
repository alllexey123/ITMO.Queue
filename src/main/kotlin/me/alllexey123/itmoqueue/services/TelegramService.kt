package me.alllexey123.itmoqueue.services

import jakarta.annotation.PostConstruct
import me.alllexey123.itmoqueue.bot.BotProperties
import org.springframework.stereotype.Service
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient
import org.telegram.telegrambots.meta.generics.TelegramClient

@Service
class TelegramService(private val botProperties: BotProperties) {

    lateinit var client: TelegramClient

    @PostConstruct
    fun init() {
        client = OkHttpTelegramClient(botProperties.token)
    }

}