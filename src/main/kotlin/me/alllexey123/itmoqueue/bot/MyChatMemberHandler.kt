package me.alllexey123.itmoqueue.bot

import jakarta.transaction.Transactional
import me.alllexey123.itmoqueue.model.Membership
import me.alllexey123.itmoqueue.services.ContextService
import me.alllexey123.itmoqueue.services.MembershipService
import me.alllexey123.itmoqueue.services.Telegram
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberUpdated

@Component
class MyChatMemberHandler(
    private val contextService: ContextService,
    private val telegram: Telegram,
    private val membershipService: MembershipService
) {


    @Transactional
    fun handle(update: ChatMemberUpdated) {
        if (update.newChatMember.status == "left") {
            onLeft(update)
        }
        if (update.newChatMember.status == "member") {
            onJoin(update)
        }
    }

    fun onLeft(update: ChatMemberUpdated) {

    }

    fun onJoin(update: ChatMemberUpdated) {
        val chatId = update.chat.id
        val from = update.from
        val membership = contextService.getMembership(chatId, from.id, from.userName)
        membershipService.resetMembershipTypes(membership.group)
        membership.type = Membership.Type.ADMIN
        val admins = telegram.execute(GetChatAdministrators.builder().chatId(chatId).build())
        admins.forEach { admin ->
            contextService.getMembership(chatId, admin.user.id, admin.user.userName).type = Membership.Type.ADMIN
        }
        val sendMessage = SendMessage.builder()
            .chatId(chatId)
            .parseMode(ParseMode.MARKDOWN)
            .text("""
                Привет 🙋
                Я - бот для создания очередей на сдачу лабораторных работ в ИТМО.
                Для начала основные моменты:
                 • Некоторые команды доступны только *текущим* админам и участнику, который меня добавил.
                 • Я не вижу все сообщения (в целях вашей же анонимности), поэтому при настройке иногда надо отвечать на моё сообщение напрямую (например, при выборе названия лабы).
                 • Бот в ранней бете (почему вы это вообще читаете?)
                 
                Для начала напишите 
            """.trimIndent())
            .build()

        telegram.execute(sendMessage)
    }

}