package me.alllexey123.itmoqueue

import jakarta.annotation.PostConstruct
import me.alllexey123.itmoqueue.bot.TelegramBot
import me.alllexey123.itmoqueue.services.GroupService
import me.alllexey123.itmoqueue.services.Telegram
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat

@SpringBootApplication
@EnableJpaAuditing
class Application(
	private val telegramBot: TelegramBot,
	private val itmoQueueProperties: ItmoQueueProperties,
	private val groupService: GroupService,
	private val telegram: Telegram
) {
	lateinit var telegramApp: TelegramBotsLongPollingApplication

	@PostConstruct
	fun init() {
		telegramApp = TelegramBotsLongPollingApplication()
		telegramApp.registerBot(itmoQueueProperties.botToken, telegramBot)

		groupService.findAll()
			.forEach { group -> {
				try {
					val getGroupInfo = GetChat.builder().chatId(group.chatId).build()
					val res = telegram.execute(getGroupInfo)
					group.name = res.title
				} catch (e: Exception) {
				}
			} }
	}

}

fun main(args: Array<String>) {
	runApplication<Application>(*args)
}
