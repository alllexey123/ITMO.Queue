package me.alllexey123.itmoqueue.bot.state

import me.alllexey123.itmoqueue.bot.MessageContext
import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.bot.ValidationResult
import me.alllexey123.itmoqueue.bot.Validators
import me.alllexey123.itmoqueue.services.Telegram
import org.springframework.stereotype.Component

@Component
class EnterNicknameStateHandler(private val telegram: Telegram, private val validators: Validators) : StateHandler() {
    override fun handle(context: MessageContext): Boolean {
        val text = context.text

        val check = validators.checkUserName(text)
        if (check is ValidationResult.Failure) {
            val send = context.sendReply()
                .text(check.msg)
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