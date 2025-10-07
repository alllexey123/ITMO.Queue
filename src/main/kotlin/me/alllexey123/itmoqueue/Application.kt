package me.alllexey123.itmoqueue

import jakarta.annotation.PostConstruct
import me.alllexey123.itmoqueue.bot.TelegramBot
import me.alllexey123.itmoqueue.services.GroupService
import me.alllexey123.itmoqueue.services.SubjectService
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication

@SpringBootApplication
@EnableJpaAuditing
class Application(
	private val telegramBot: TelegramBot,
	private val itmoQueueProperties: ItmoQueueProperties,
	private val groupService: GroupService,
	private val subjectService: SubjectService
) {
	lateinit var telegramApp: TelegramBotsLongPollingApplication

	@PostConstruct
	fun init() {
		telegramApp = TelegramBotsLongPollingApplication()
		telegramApp.registerBot(itmoQueueProperties.botToken, telegramBot)

		subjectService.generateUniqueShortIdForSubjects()
		groupService.refreshGroupNames()
	}

}

fun main(args: Array<String>) {
	runApplication<Application>(*args)
}
