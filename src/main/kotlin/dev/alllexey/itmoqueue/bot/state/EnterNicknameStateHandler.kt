package dev.alllexey.itmoqueue.bot.state

import dev.alllexey.itmoqueue.bot.MessageContext
import dev.alllexey.itmoqueue.bot.Scope
import dev.alllexey.itmoqueue.bot.ValidationResult
import dev.alllexey.itmoqueue.bot.Validators
import dev.alllexey.itmoqueue.services.Telegram
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