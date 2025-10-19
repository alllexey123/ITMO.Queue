package dev.alllexey.itmoqueue.bot.view

import dev.alllexey.itmoqueue.bot.Emoji
import dev.alllexey.itmoqueue.bot.callback.CallbackData
import dev.alllexey.itmoqueue.bot.extensions.inlineButton
import dev.alllexey.itmoqueue.bot.extensions.row
import dev.alllexey.itmoqueue.model.User
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup

@Component
class LabAddToQueueView {

    fun getLabAttemptText(user: User, isPrivate: Boolean): String {
        return if (isPrivate) "Какая это попытка?"
        else "${user.mention()}, какая это попытка?\n\n_У вас 1 минута на выбор, потом сообщение могут удалить_"
    }

    fun getLabAttemptKeyboard(): InlineKeyboardMarkup {
        val count = 4
        val buttons = (1..count).map { i ->
            val text = if (i == count) "$i+" else "$i"
            selectAttempt(i, text)
        } + cancel

        return InlineKeyboardMarkup(listOf(row(buttons)))
    }

    companion object Buttons {
        val cancel = inlineButton(Emoji.CANCEL, CallbackData.LabAddToQueueCancel())
        val selectAttempt = {i: Int, s: String -> inlineButton(s, CallbackData.LabAddToQueueAttempt(i))}
    }
}