package me.alllexey123.itmoqueue.bot.command

import me.alllexey123.itmoqueue.bot.MessageContext
import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.services.Telegram
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
                Рекомендую прочитать инструкцию [тут](https://telegra.ph/Instrukciya-dlya-bota-ITMOQueue-10-04)
            """.trimIndent())
            .replyMarkup(buildKeyboard())
            .parseMode(ParseMode.MARKDOWN)
            .build()
        telegram.execute(send)
    }

    fun buildKeyboard(): ReplyKeyboardMarkup {
        return ReplyKeyboardMarkup.builder()
            .keyboardRow(KeyboardRow("/labs", "/name"))
            .resizeKeyboard(true)
            .build()
    }

    override fun command() = Command.START

    override fun scope() = Scope.ANY
}