package me.alllexey123.itmoqueue.bot.command

import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.bot.callback.CallbackHandler
import me.alllexey123.itmoqueue.bot.extensions.edit
import me.alllexey123.itmoqueue.bot.extensions.inlineButton
import me.alllexey123.itmoqueue.bot.extensions.withInlineKeyboard
import me.alllexey123.itmoqueue.bot.state.EditLabNameState
import me.alllexey123.itmoqueue.bot.state.EditSubjectState
import me.alllexey123.itmoqueue.bot.state.StateManager
import me.alllexey123.itmoqueue.model.LabWork
import me.alllexey123.itmoqueue.services.GroupService
import me.alllexey123.itmoqueue.services.LabWorkService
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
class ListLabsCommand(
    private val groupService: GroupService,
    private val telegramService: TelegramService,
    private val stateManager: StateManager,
    private val labWorkService: LabWorkService,
    private val editLabNameState: EditLabNameState
) :
    CommandHandler, CallbackHandler {

    override fun handle(message: Message) {
        val chat = message.chat
        val group = groupService.getOrCreateByChatId(chat.id)
        val labs = group.labs
        val sendMessage = SendMessage.builder()
            .chatId(message.chat.id)
            .withInlineKeyboard(getListMessageText(labs), getListKeyboard(labs))

        telegramService.client.execute(sendMessage)
    }

    val perPage = 9

    fun getListMessageText(labs: List<LabWork>, page: Int = 1): String {
        val pageLabs = labs.drop((page - 1) * perPage).take(perPage)

        return buildString {
            if (pageLabs.isNotEmpty()) {
                appendLine("Список лаб:\n")
                labs.forEachIndexed { i, lab ->
                    appendLine("${i + 1}. ${lab.name}")
                }
                appendLine()
            } else {
                appendLine("Пока тут пусто\n")
            }
            appendLine("Добавить лабу - /new_lab")
        }
    }


    fun getListKeyboard(labs: List<LabWork>, perRow: Int = 3, page: Int = 1): InlineKeyboardMarkup {
        val pageLabs = labs.drop((page - 1) * perPage).take(perPage)

        val rows = pageLabs.chunked(perRow).map { chunk ->
            InlineKeyboardRow(
                chunk.mapIndexed { i, lab ->
                    inlineButton((labs.indexOf(lab) + 1).toString(), addPrefix("select ${lab.id}"))
                }
            )
        }.toMutableList()

        val pagination = mutableListOf<InlineKeyboardButton>()
        if (page > 1) {
            pagination.add(inlineButton("⬅\uFE0F", addPrefix("lab_page ${page - 1}")))
        }
        if (page * perPage < labs.size) {
            pagination.add(inlineButton("➡\uFE0F", addPrefix("lab_page ${page + 1}")))
        }
        if (pagination.isNotEmpty()) {
            rows.add(InlineKeyboardRow(pagination))
        }

        return InlineKeyboardMarkup(rows)
    }


    fun getLabText(lab: LabWork?): String {
        if (lab == null) {
            return "Лаба не найдена"
        }

        return buildString {
            appendLine("Лаба <strong>${lab.name}</strong>")
            appendLine("Предмет: <strong>${lab.subject?.name ?: "неизвестный"}</strong>")
        }
    }

    fun getLabKeyboard(lab: LabWork?): InlineKeyboardMarkup {
        if (lab == null) {
            return InlineKeyboardMarkup.builder().build()
        }

        val rows = mutableListOf<InlineKeyboardRow>()
        rows.add(
            InlineKeyboardRow(
                listOf(
                    InlineKeyboardButton.builder()
                        .text("Изменить")
                        .callbackData(addPrefix("edit ${lab.id}"))
                        .build(),
                    InlineKeyboardButton.builder()
                        .text("Удалить")
                        .callbackData(addPrefix("delete ${lab.id}"))
                        .build(),
                    InlineKeyboardButton.builder()
                        .text("Назад")
                        .callbackData(addPrefix("main"))
                        .build(),
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
                val lab = labWorkService.findById(split[1].toLong())
                val editMessage = EditMessageText.builder()
                    .edit(message)
                    .parseMode(ParseMode.HTML)
                    .withInlineKeyboard(getLabText(lab), getLabKeyboard(lab))

                telegramService.client.execute(editMessage)
            }

            "delete" -> {
                labWorkService.deleteById(split[1].toLong())
                labWorkService.flush()

                updateLabsList(message)
            }

            "edit" -> {
                val labId = split[1].toLong()
                val editMessage = EditMessageText.builder()
                    .edit(message)
                    .text("Введите новое название лабы (ответом на это сообщение):")
                    .build()

                editLabNameState.setChatData(chat.id, labId.toString())
                stateManager.setHandler(chat.id, editLabNameState)

                telegramService.client.execute(editMessage)
            }

            "main" -> {
                updateLabsList(message)
            }

            "lab_page" -> {
                val page = split[1].toInt()
                val labs = groupService.getOrCreateByChatId(chat.id).labs

                val editMessage = EditMessageText.builder()
                    .edit(message)
                    .withInlineKeyboard(getListMessageText(labs, page), getListKeyboard(labs, page = page))

                telegramService.client.execute(editMessage)
            }
        }
    }

    fun updateLabsList(message: MaybeInaccessibleMessage) {
        val labs = groupService.getOrCreateByChatId(message.chatId).labs
        val editMessage = EditMessageText.builder()
            .edit(message)
            .withInlineKeyboard(getListMessageText(labs), getListKeyboard(labs))
        telegramService.client.execute(editMessage)
    }


    override fun prefix() = command()

    override fun command() = "list_labs"

    override fun scope() = Scope.GROUP
}