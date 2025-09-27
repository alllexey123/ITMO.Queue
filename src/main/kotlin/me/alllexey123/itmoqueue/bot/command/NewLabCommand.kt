package me.alllexey123.itmoqueue.bot.command

import me.alllexey123.itmoqueue.bot.Emoji
import me.alllexey123.itmoqueue.bot.MessageContext
import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.bot.callback.CallbackContext
import me.alllexey123.itmoqueue.bot.callback.CallbackHandler
import me.alllexey123.itmoqueue.model.Subject
import me.alllexey123.itmoqueue.services.Telegram
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow
import me.alllexey123.itmoqueue.bot.extensions.*
import me.alllexey123.itmoqueue.bot.state.EnterLabNameState
import me.alllexey123.itmoqueue.bot.state.StateManager

@Component
class NewLabCommand(
    private val telegram: Telegram,
    private val stateManager: StateManager,
    private val enterLabNameState: EnterLabNameState
) :
    CommandHandler, CallbackHandler {

    override fun handle(context: MessageContext) {
        val group = context.group!!
        val send = context.sendReply()

        val subjects = group.subjects
        if (subjects.isNotEmpty()) {
            send.withInlineKeyboard("Выберите предмет: ", getSubjectListButtons(subjects))
        } else {
            send.text("Не добавлен ни один предмет\n\nДобавить предмет - /new_subject")
        }

        telegram.execute(send.build())
    }

    fun getSubjectListButtons(subjects: List<Subject>, page: Int = 1): InlineKeyboardMarkup {
        val perPage = 5

        val pageSubjects = subjects.drop((page - 1) * perPage).take(perPage)

        val rows = pageSubjects.map { subject ->
            inlineRowButton(subject.name, encode("subject", subject.id))
        }.toMutableList()

        val pagination = mutableListOf<InlineKeyboardButton>()
        if (page > 1) {
            pagination.add(inlineButton(Emoji.ARROW_RIGHT, encode("subject_page", page - 1)))
        }
        if (page * perPage < subjects.size) {
            pagination.add(inlineButton(Emoji.ARROW_RIGHT, encode("subject_page", page + 1)))
        }
        if (pagination.isNotEmpty()) {
            rows.add(InlineKeyboardRow(pagination))
        }

        rows.add(inlineRowButton(Emoji.CANCEL, encode("cancel")))

        return InlineKeyboardMarkup(rows)
    }


    override fun handle(context: CallbackContext) {
        when (context.asString(0)) {
            "subject" -> {
                val editMessage = EditMessageText.builder()
                    .edit(context.message)
                    .text("Введите название лабы (ответом на это сообщение):")
                    .build()

                enterLabNameState.setChatData(context.chatId, context.asString(1))
                stateManager.setHandler(context.chatId, enterLabNameState)

                telegram.execute(editMessage)
            }

            "subject_page" -> {
                val page = context.asInt(1)
                val editMessage = EditMessageText.builder()
                    .edit(context.message)
                    .withInlineKeyboard(
                        "Выберите предмет (стр. $page): ",
                        getSubjectListButtons(context.group!!.subjects, page)
                    )

                telegram.execute(editMessage)
            }

            "cancel" -> {
                telegram.execute(
                    EditMessageText.builder()
                        .text("Создание лабы отменено")
                        .edit(context.message)
                        .build()
                )
            }
        }
    }

    override fun prefix() = command()

    override fun command() = "new_lab"

    override fun scope() = Scope.GROUP
}