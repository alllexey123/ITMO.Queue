package me.alllexey123.itmoqueue.bot.command

import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.services.GroupService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.message.Message

@Component
class ListLabsCommand(private val groupService: GroupService) : CommandHandler {

    override fun handle(message: Message) {
        val chat = message.chat
        val group = groupService.getOrCreateByChatId(chat.id)
        TODO()
    }

    override fun command() = "list_labs"

    override fun scope() = Scope.GROUP
}