package dev.alllexey.itmoqueue.bot.command

import dev.alllexey.itmoqueue.bot.MessageContext
import dev.alllexey.itmoqueue.bot.Scope
import dev.alllexey.itmoqueue.services.Telegram
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow

@Component
class StartCommand(private val telegram: Telegram) : CommandHandler {

    override fun handleMessage(context: MessageContext) {
        if (!context.isPrivate) return
        val send = context.send()
            .text("""
                Привет 🙋
                Я - бот для создания очередей на сдачу лабораторных работ в ИТМО.
                Рекомендую прочитать общую информацию о боте [тут](https://github.com/alllexey123/ITMO.Queue)
            """.trimIndent())
            .replyMarkup(buildKeyboard())
            .parseMode(ParseMode.MARKDOWN)
            .build()
        telegram.execute(send)
    }

    fun buildKeyboard(): ReplyKeyboardMarkup {
        return ReplyKeyboardMarkup.builder()
            .keyboardRow(KeyboardRow(Command.GROUP.raw, Command.LABS.raw, Command.NAME.raw))
            .resizeKeyboard(true)
            .isPersistent(true)
            .build()
    }

    override fun command() = Command.START

    override fun scope() = Scope.ANY
}