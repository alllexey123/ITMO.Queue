package dev.alllexey.itmoqueue.bot

import jakarta.transaction.Transactional
import dev.alllexey.itmoqueue.bot.command.Command
import dev.alllexey.itmoqueue.model.Membership
import dev.alllexey.itmoqueue.services.ContextService
import dev.alllexey.itmoqueue.services.MembershipService
import dev.alllexey.itmoqueue.services.Telegram
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.ParseMode
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
        if (update.newChatMember.status == "member") {
            onJoin(update)
        }
    }

    fun onJoin(update: ChatMemberUpdated) {
        val chatId = update.chat.id
        val from = update.from
        if (update.chat.isUserChat) {
            return
        }
        val membership = contextService.getMembership(chatId, update.chat.title, from.id, from.userName)
        membershipService.resetMembershipTypes(membership.group)
        membership.type = Membership.Type.ADMIN
        val sendMessage = SendMessage.builder()
            .chatId(chatId)
            .parseMode(ParseMode.MARKDOWN)
            .text("""
                Привет 🙋
                Я - бот для создания очередей на сдачу лабораторных работ в ИТМО.
                Для начала основные моменты:
                 • Некоторые команды доступны только участнику, который меня добавил (он считается админом).
                   Чтобы изменить участника-администратора, меня можно спокойно удалить и добавить обратно, данные не будут утеряны.
                 • Я не вижу все сообщения (в целях вашей же анонимности), поэтому при настройке иногда надо отвечать на моё сообщение напрямую (например, при выборе названия лабы).
                 • Исходный код бота полностью открыт и находится [тут](https://github.com/alllexey123/ITMO.Queue)
                 
                Основная информация о боте [тут](https://github.com/alllexey123/ITMO.Queue)
                Для начала напишите ${Command.GROUP.escaped} или ${Command.SUBJECTS.escaped}
            """.trimIndent())
            .disableWebPagePreview(true)
            .build()

        telegram.execute(sendMessage)
    }

}