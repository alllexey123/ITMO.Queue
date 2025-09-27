package me.alllexey123.itmoqueue.bot.state

import me.alllexey123.itmoqueue.bot.MessageContext
import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.services.Telegram
import org.springframework.stereotype.Component

@Component
class EnterNicknameStateHandler(private val telegram: Telegram) : StateHandler() {
    override fun handle(context: MessageContext): Boolean {
        val text = context.text

        if (text.length > 15) {
            val send = context.sendReply()
                .text("Слишком длинный никнейм (максимум - 15 символов)")
                .build()
            telegram.execute(send)
            return false
        } else {
            context.user.nickname = text
            val send = context.sendReply()
                .text("Ник успешно изменён!")
                .build()
            telegram.execute(send)
            return true
        }
    }

    override fun scope() = Scope.USER
}