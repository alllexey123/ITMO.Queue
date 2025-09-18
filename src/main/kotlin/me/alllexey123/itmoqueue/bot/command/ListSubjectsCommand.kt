package me.alllexey123.itmoqueue.bot.command

import me.alllexey123.itmoqueue.bot.Emoji
import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.bot.callback.CallbackHandler
import me.alllexey123.itmoqueue.bot.extensions.edit
import me.alllexey123.itmoqueue.bot.extensions.inlineButton
import me.alllexey123.itmoqueue.bot.extensions.inlineRowButton
import me.alllexey123.itmoqueue.bot.extensions.withInlineKeyboard
import me.alllexey123.itmoqueue.bot.state.EditSubjectState
import me.alllexey123.itmoqueue.bot.state.StateManager
import me.alllexey123.itmoqueue.model.Subject
import me.alllexey123.itmoqueue.services.GroupService
import me.alllexey123.itmoqueue.services.SubjectService
import me.alllexey123.itmoqueue.services.TelegramService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow

@Component
class ListSubjectsCommand(
    private val groupService: GroupService,
    private val telegramService: TelegramService,
    private val subjectService: SubjectService,
    private val editSubjectState: EditSubjectState,
    private val stateManager: StateManager
) :
    CommandHandler, CallbackHandler {

    override fun handle(message: Message) {
        val chat = message.chat
        val group = groupService.getOrCreateByChatId(chat.id)
        val subjects = group.subjects
        val sendMessage = SendMessage.builder()
            .chatId(message.chat.id)
            .withInlineKeyboard(getListMessageText(subjects), getListKeyboard(subjects))

        telegramService.client.execute(sendMessage)
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
            appendLine("Добавить предмет - /new_subject")
        }
    }


    fun getListKeyboard(subjects: List<Subject>, perRow: Int = 3): InlineKeyboardMarkup {
        val rows = subjects.chunked(perRow).map { chunk ->
            InlineKeyboardRow(
                chunk.mapIndexed { i, subject ->
                    InlineKeyboardButton.builder()
                        .text((subjects.indexOf(subject) + 1).toString())
                        .callbackData(addPrefix("select ${subject.id}"))
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
                    inlineButton(Emoji.BACK, addPrefix("main")),
                    inlineButton(Emoji.EDIT, addPrefix("edit ${subject.id}")),
                    inlineButton(Emoji.DELETE, addPrefix("delete ${subject.id}"))
                )
            )
        )
        return InlineKeyboardMarkup.builder().keyboard(rows).build()
    }

    override fun handle(callbackQuery: CallbackQuery) {
        val split = removePrefix(callbackQuery.data).split(" ")
        val message = callbackQuery.message
        val chat = message.chat
        when (split[0]) {
            "select" -> {
                val subject = subjectService.findById(split[1].toLong())
                val editMessage = EditMessageText.builder()
                    .edit(message)
                    .parseMode(ParseMode.MARKDOWN)
                    .withInlineKeyboard(getSubjectText(subject), getSubjectKeyboard(subject))

                telegramService.client.execute(editMessage)
            }

            "delete" -> {
                subjectService.deleteById(split[1].toLong())
                subjectService.flush()

                updateSubjectList(message)
            }

            "edit" -> {
                val subjectId = split[1].toLong()
                val editMessage = EditMessageText.builder()
                    .edit(message)
                    .text("Введите новое название предмета (ответом на это сообщение):")
                    .replyMarkup(
                        InlineKeyboardMarkup.builder().keyboardRow(
                            inlineRowButton(Emoji.BACK, addPrefix("select $subjectId"))
                        ).build()
                    )
                    .build()

                editSubjectState.setChatData(chat.id, subjectId.toString())
                stateManager.setHandler(chat.id, editSubjectState)

                telegramService.client.execute(editMessage)
            }

            "main" -> {
                updateSubjectList(message)
            }
        }
    }

    fun updateSubjectList(message: MaybeInaccessibleMessage) {
        val subjects = groupService.getOrCreateByChatId(message.chatId).subjects
        val editMessage = EditMessageText.builder()
            .edit(message)
            .withInlineKeyboard(getListMessageText(subjects), getListKeyboard(subjects))
        telegramService.client.execute(editMessage)
    }


    override fun prefix() = command()

    override fun command() = "list_subjects"

    override fun scope() = Scope.GROUP
}