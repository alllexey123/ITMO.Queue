package me.alllexey123.itmoqueue.bot.command

import me.alllexey123.itmoqueue.bot.Emoji
import me.alllexey123.itmoqueue.bot.MessageContext
import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.bot.callback.CallbackContext
import me.alllexey123.itmoqueue.bot.extensions.edit
import me.alllexey123.itmoqueue.bot.extensions.inlineButton
import me.alllexey123.itmoqueue.bot.extensions.inlineRowButton
import me.alllexey123.itmoqueue.bot.extensions.withInlineKeyboard
import me.alllexey123.itmoqueue.bot.state.EditLabNameState
import me.alllexey123.itmoqueue.bot.state.StateManager
import me.alllexey123.itmoqueue.model.LabWork
import me.alllexey123.itmoqueue.services.LabWorkService
import me.alllexey123.itmoqueue.services.QueueService
import me.alllexey123.itmoqueue.services.Telegram
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow
import java.time.LocalDateTime

@Component
class GroupListLabsCommand(
    private val telegram: Telegram,
    private val stateManager: StateManager,
    private val labWorkService: LabWorkService,
    private val editLabNameState: EditLabNameState,
    queueService: QueueService,
) : BaseListLabsCommand(telegram, labWorkService, queueService) {

    private val perPage = 9
    private val perRow = 3

    override fun handle(context: MessageContext) {
        val group = context.group!!
        val labs = group.labs
        val sendMessage = context.send()
            .withInlineKeyboard(getListMessageText(labs), getListKeyboard(labs))
        telegram.execute(sendMessage)
    }

    override fun handle(context: CallbackContext) {
        super.handle(context)
        when (context.asString(0)) {
            "delete" -> handleLabDeleteQuery(context)
            "edit" -> handleLabEditQuery(context)
            "lab_page" -> handleLabPageQuery(context)
            "pin_lab_data" -> handleLabDataPinQuery(context)
        }
    }

    private fun getListMessageText(labs: List<LabWork>, page: Int = 1): String {
        val pageLabs = labs.drop((page - 1) * perPage).take(perPage)
        return buildString {
            appendLine("Список лаб в этой группе:\n")
            if (pageLabs.isNotEmpty()) {
                pageLabs.forEachIndexed { i, lab ->
                    val labIndex = (page - 1) * perPage + i + 1
                    appendLine("$labIndex. ${lab.name}")
                }
            } else {
                appendLine("Пока тут пусто\n")
            }
            appendLine("\nДобавить лабу - /${GroupNewLabCommand.NAME}")
        }
    }

    private fun getListKeyboard(labs: List<LabWork>, page: Int = 1): InlineKeyboardMarkup {
        val pageLabs = labs.drop((page - 1) * perPage).take(perPage)
        val rows = pageLabs.chunked(perRow).mapIndexed { i, chunk ->
            InlineKeyboardRow(
                chunk.mapIndexed { j, lab ->
                    val labIndex = (page - 1) * perPage + i * perRow + j + 1
                    inlineButton(labIndex.toString(), encode("select", lab.id, LocalDateTime.MIN, false))
                }
            )
        }.toMutableList()

        val pagination = mutableListOf<InlineKeyboardButton>()
        if (page > 1) {
            pagination.add(inlineButton(Emoji.ARROW_LEFT, encode("lab_page", page - 1)))
        }
        if (page * perPage < labs.size) {
            pagination.add(inlineButton(Emoji.ARROW_RIGHT, encode("lab_page", page + 1)))
        }

        if (pagination.isNotEmpty()) {
            rows.add(InlineKeyboardRow(pagination))
        }
        return InlineKeyboardMarkup(rows)
    }

    private fun handleLabPageQuery(context: CallbackContext) {
        val page = context.asInt(1)
        val labs = context.group!!.labs
        val editMessage = EditMessageText.builder()
            .edit(context.message)
            .withInlineKeyboard(getListMessageText(labs, page), getListKeyboard(labs, page = page))
        telegram.execute(editMessage)
    }

    private fun handleLabEditQuery(context: CallbackContext) {
        if (!context.requireAdmin(telegram)) return
        val labId = context.asLong(1)
        val editMessage = EditMessageText.builder()
            .edit(context.message)
            .text("Введите новое название лабы (ответом на это сообщение):")
            .replyMarkup(
                InlineKeyboardMarkup.builder().keyboardRow(
                    inlineRowButton(Emoji.BACK, encode("select", labId, LocalDateTime.now(), false))
                ).build()
            )
            .build()
        editLabNameState.setChatData(context.chatId, labId)
        stateManager.setHandler(context.chatId, editLabNameState)
        telegram.execute(editMessage)
    }

    private fun handleLabDeleteQuery(context: CallbackContext) {
        if (!context.requireAdmin(telegram)) return
        labWorkService.deleteById(context.asLong(1))
        updateLabsList(context)
    }

    private fun handleLabDataPinQuery(context: CallbackContext) {
        if (!context.requireAdmin(telegram)) return
        val lab = labWorkService.findById(context.asLong(1))
        editLabDataMessage(context.chatId, context.messageId, lab, true)
    }

    override fun getLabKeyboard(lab: LabWork?, pinned: Boolean): InlineKeyboardMarkup {
        if (lab == null) return InlineKeyboardMarkup.builder().build()

        val rows = mutableListOf(
            InlineKeyboardRow(
                listOf(
                    inlineButton(Emoji.PLUS, encode("add_to_queue", lab.id, pinned)),
                    inlineButton(Emoji.MINUS, encode("remove_from_queue", lab.id, pinned)),
                    inlineButton(Emoji.REFRESH, encode("select", lab.id, LocalDateTime.now(), pinned)),
                )
            )
        )

        if (pinned) {
            rows.add(
                InlineKeyboardRow(
                    inlineButton(Emoji.CHECK, encode("mark_done", lab.queues[0].id, pinned))
                )
            )
        } else {
            rows.add(
                InlineKeyboardRow(
                    listOf(
                        inlineButton(Emoji.BACK, encode("main")),
                        inlineButton(Emoji.EDIT, encode("edit", lab.id)),
                        inlineButton(Emoji.DELETE, encode("delete", lab.id))
                    )
                )
            )
            rows.add(
                InlineKeyboardRow(
                    listOf(
                        inlineButton(Emoji.CHECK, encode("mark_done", lab.queues[0].id, pinned)),
                        inlineButton(Emoji.PIN, encode("pin_lab_data", lab.id))
                    )
                )
            )
        }

        return InlineKeyboardMarkup(rows)
    }

    override fun editLabDataMessage(chatId: Long, messageId: Int, lab: LabWork?, pinned: Boolean) {
        val editMessage = EditMessageText.builder()
            .chatId(chatId)
            .messageId(messageId)
            .parseMode(ParseMode.MARKDOWN)
            .withInlineKeyboard(getLabText(lab), getLabKeyboard(lab, pinned))
        telegram.execute(editMessage)
    }

    override fun updateLabsList(context: CallbackContext) {
        val labs = context.group!!.labs
        val editMessage = EditMessageText.builder()
            .edit(context.message)
            .withInlineKeyboard(getListMessageText(labs), getListKeyboard(labs))
        telegram.execute(editMessage)
    }

    override fun command() = NAME
    override fun prefix() = command()
    override fun scope() = Scope.GROUP

    companion object {
        const val NAME = "list_labs"
    }
}