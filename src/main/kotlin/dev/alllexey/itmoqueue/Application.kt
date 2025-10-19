package dev.alllexey.itmoqueue

import jakarta.annotation.PostConstruct
import dev.alllexey.itmoqueue.bot.TelegramBot
import dev.alllexey.itmoqueue.services.GroupService
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication

@SpringBootApplication
@EnableJpaAuditing
class Application(
	private val telegramBot: TelegramBot,
	private val itmoQueueProperties: ItmoQueueProperties,
	private val groupService: GroupService
) {
	lateinit var telegramApp: TelegramBotsLongPollingApplication

	@PostConstruct
	fun init() {
		telegramApp = TelegramBotsLongPollingApplication()
		telegramApp.registerBot(itmoQueueProperties.botToken, telegramBot)

		groupService.refreshGroupNames()
	}

}

fun main(args: Array<String>) {
	runApplication<Application>(*args)
}
