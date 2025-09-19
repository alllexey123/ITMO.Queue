package me.alllexey123.itmoqueue.services

import me.alllexey123.itmoqueue.bot.BotProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient
import org.telegram.telegrambots.meta.generics.TelegramClient

@Configuration
class TelegramClientConfig {

    @Bean
    fun telegramClient(botProperties: BotProperties): TelegramClient {
        return OkHttpTelegramClient(botProperties.token)
    }
}