package me.alllexey123.itmoqueue

import jakarta.annotation.PostConstruct
import me.alllexey123.itmoqueue.bot.BotProperties
import me.alllexey123.itmoqueue.bot.TelegramBot
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication

@SpringBootApplication
class Application(private val telegramBot: TelegramBot, private val botProperties: BotProperties) {
	lateinit var telegramApp: TelegramBotsLongPollingApplication

	@PostConstruct
	fun init() {
		telegramApp = TelegramBotsLongPollingApplication()
		telegramApp.registerBot(botProperties.token, telegramBot)
	}

}

fun main(args: Array<String>) {
	runApplication<Application>(*args)
}
