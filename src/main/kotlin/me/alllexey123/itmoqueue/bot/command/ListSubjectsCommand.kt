package me.alllexey123.itmoqueue.bot.command

import me.alllexey123.itmoqueue.bot.Emoji
import me.alllexey123.itmoqueue.bot.MessageContext
import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.bot.callback.CallbackContext
import me.alllexey123.itmoqueue.bot.callback.CallbackHandler
import me.alllexey123.itmoqueue.bot.extensions.edit
import me.alllexey123.itmoqueue.bot.extensions.inlineButton
import me.alllexey123.itmoqueue.bot.extensions.inlineRowButton
import me.alllexey123.itmoqueue.bot.extensions.withInlineKeyboard
import me.alllexey123.itmoqueue.bot.state.EditSubjectState
import me.alllexey123.itmoqueue.bot.state.StateManager
import me.alllexey123.itmoqueue.model.Subject
import me.alllexey123.itmoqueue.services.SubjectService
import me.alllexey123.itmoqueue.services.Telegram
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow

@Component
class ListSubjectsCommand(
    private val telegram: Telegram,
    private val subjectService: SubjectService,
    private val editSubjectState: EditSubjectState,
    private val stateManager: StateManager,
) :
    CommandHandler, CallbackHandler {

    override fun handle(context: MessageContext) {
        val group = context.group!!
        val subjects = group.subjects
        val sendMessage = context.send()
            .withInlineKeyboard(getListMessageText(subjects), getListKeyboard(subjects))

        telegram.execute(sendMessage)
    }

    fun getListMessageText(subjects: List<Subject>): String {
        return buildString {
            if (subjects.isNotEmpty()) {
                appendLine("Список предметов:\n")
                subjects.forEachIndexed { i, subject ->
                    appendLine("${i + 1}. ${subject.name}")
                }
                appendLine()
            } else {
                appendLine("Пока тут пусто\n")
            }
            appendLine("Добавить предмет - /${NewSubjectCommand.NAME}")
        }
    }


    fun getListKeyboard(subjects: List<Subject>, perRow: Int = 3): InlineKeyboardMarkup {
        val rows = subjects.chunked(perRow).map { chunk ->
            InlineKeyboardRow(
                chunk.mapIndexed { i, subject ->
                    InlineKeyboardButton.builder()
                        .text((subjects.indexOf(subject) + 1).toString())
                        .callbackData(encode("select", subject.id))
                        .build()
                }
            )
        }
        return InlineKeyboardMarkup(rows)
    }


    fun getSubjectText(subject: Subject?): String {
        if (subject == null) {
            return "Предмет не найден"
        }

        return buildString {
            appendLine("Предмет \"*${subject.name}*\"")

            if (subject.labWorks.isEmpty()) {
                appendLine("Лабораторных работ пока не было")
            } else {
                appendLine("Лабы: ")
                subject.labWorks.forEachIndexed { i, labWork ->
                    appendLine("${i + 1}. ${labWork.name}")
                }
            }
        }
    }

    fun getSubjectKeyboard(subject: Subject?): InlineKeyboardMarkup {
        if (subject == null) {
            return InlineKeyboardMarkup.builder().build()
        }

        val rows = mutableListOf<InlineKeyboardRow>()
        rows.add(
            InlineKeyboardRow(
                listOf(
                    inlineButton(Emoji.BACK, encode("main")),
                    inlineButton(Emoji.EDIT, encode("edit", subject.id)),
                    inlineButton(Emoji.DELETE, encode("delete", subject.id))
                )
            )
        )
        return InlineKeyboardMarkup.builder().keyboard(rows).build()
    }

    override fun handle(context: CallbackContext) {
        when (context.asString(0)) {
            "select" -> {
                val subject = subjectService.findById(context.asLong(1))
                val editMessage = EditMessageText.builder()
                    .edit(context.message)
                    .parseMode(ParseMode.MARKDOWN)
                    .withInlineKeyboard(getSubjectText(subject), getSubjectKeyboard(subject))

                telegram.execute(editMessage)
            }

            "delete" -> {
                subjectService.deleteById(context.asLong(1))

                updateSubjectList(context)
            }

            "edit" -> {
                val subjectId = context.asLong(1)
                val editMessage = EditMessageText.builder()
                    .edit(context.message)
                    .text("Введите новое название предмета (ответом на это сообщение):")
                    .replyMarkup(
                        InlineKeyboardMarkup.builder().keyboardRow(
                            inlineRowButton(Emoji.BACK, encode("select", subjectId))
                        ).build()
                    )
                    .build()

                editSubjectState.setChatData(context.chatId, subjectId)
                stateManager.setHandler(context.chatId, editSubjectState)

                telegram.execute(editMessage)
            }

            "main" -> {
                updateSubjectList(context)
            }
        }
    }

    fun updateSubjectList(context: CallbackContext) {
        val subjects = context.group!!.subjects
        val editMessage = EditMessageText.builder()
            .edit(context.message)
            .withInlineKeyboard(getListMessageText(subjects), getListKeyboard(subjects))
        telegram.execute(editMessage)
    }

    override fun prefix() = command()

    override fun command() = NAME

    override fun scope() = Scope.GROUP

    companion object {
        const val NAME = "list_subjects"
    }
}