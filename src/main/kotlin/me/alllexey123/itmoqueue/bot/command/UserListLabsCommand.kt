package me.alllexey123.itmoqueue.bot.command

import me.alllexey123.itmoqueue.bot.Emoji
import me.alllexey123.itmoqueue.bot.MessageContext
import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.bot.callback.CallbackContext
import me.alllexey123.itmoqueue.bot.extensions.*
import me.alllexey123.itmoqueue.model.LabWork
import me.alllexey123.itmoqueue.model.QueueEntry
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
class UserListLabsCommand(
    private val telegram: Telegram,
    labWorkService: LabWorkService,
    queueService: QueueService
) : BaseListLabsCommand(telegram, labWorkService, queueService) {

    private val perPage = 9
    private val perRow = 3

    override fun handle(context: MessageContext) {
        val user = context.user
        val queueEntries = user.queueEntries
            .filter { !it.done }
        val sendMessage = context.send()
            .parseMode(ParseMode.MARKDOWN)
            .withInlineKeyboard(getListMessageText(queueEntries), getListKeyboard(queueEntries))
        telegram.execute(sendMessage)
    }

    override fun handle(context: CallbackContext) {
        super.handle(context)
        when (context.asString(0)) {
            "entry_page" -> handleEntryPageQuery(context)
        }
    }

    private fun getListMessageText(entries: List<QueueEntry>, page: Int = 1): String {
        val pageEntries = entries.drop((page - 1) * perPage).take(perPage)
        return buildString {
            appendLine("Список ваших активных лаб:\n")
            if (pageEntries.isNotEmpty()) {
                pageEntries.forEachIndexed { i, entry ->
                    val labIndex = (page - 1) * perPage + i + 1
                    val lab = entry.queue.labWork
                    val status = if (entry.done) Emoji.CHECK else Emoji.CANCEL
                    appendLine("`$labIndex. ${lab.name} (${lab.subject.name}) [$status]`")
                }
            } else {
                appendLine("Пока тут пусто...\n")
            }
        }
    }

    private fun getListKeyboard(entries: List<QueueEntry>, page: Int = 1): InlineKeyboardMarkup {
        val pageEntries = entries.drop((page - 1) * perPage).take(perPage)
        val rows = pageEntries.chunked(perRow).mapIndexed { i, chunk ->
            InlineKeyboardRow(
                chunk.mapIndexed { j, entry ->
                    val labIndex = (page - 1) * perPage + i * perRow + j + 1
                    inlineButton(labIndex.toString(), encode("select", entry.queue.labWork.id, LocalDateTime.MIN, false))
                }
            )
        }.toMutableList()

        val pagination = mutableListOf<InlineKeyboardButton>()
        if (page > 1) {
            pagination.add(inlineButton(Emoji.ARROW_LEFT, encode("entry_page", page - 1)))
        }
        if (page * perPage < entries.size) {
            pagination.add(inlineButton(Emoji.ARROW_RIGHT, encode("entry_page", page + 1)))
        }

        if (pagination.isNotEmpty()) {
            rows.add(InlineKeyboardRow(pagination))
        }

        return InlineKeyboardMarkup(rows)
    }

    private fun handleEntryPageQuery(context: CallbackContext) {
        val page = context.asInt(1)
        val queueEntries = context.user.queueEntries.filter { !it.done }
        val editMessage = EditMessageText.builder()
            .edit(context.message)
            .parseMode(ParseMode.MARKDOWN)
            .withInlineKeyboard(getListMessageText(queueEntries, page), getListKeyboard(queueEntries, page))
        telegram.execute(editMessage)
    }

    override fun getLabKeyboard(lab: LabWork?, pinned: Boolean): InlineKeyboardMarkup {
        if (lab == null) return InlineKeyboardMarkup.builder().build()

        return InlineKeyboardMarkup.builder().keyboard(
            listOf(
                InlineKeyboardRow(
                    listOf(
                        inlineButton(Emoji.PLUS, encode("add_to_queue", lab.id, pinned)),
                        inlineButton(Emoji.MINUS, encode("remove_from_queue", lab.id, pinned)),
                        inlineButton(Emoji.REFRESH, encode("select", lab.id, LocalDateTime.now(), pinned)),
                    )
                ),
                InlineKeyboardRow(
                    listOf(
                        inlineButton(Emoji.BACK, encode("main")),
                        inlineButton(Emoji.CHECK, encode("mark_done", lab.queues[0].id, pinned)),
                    )
                )
            )
        ).build()
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
        val queueEntries = context.user.queueEntries.filter { !it.done }
        val editMessage = EditMessageText.builder()
            .edit(context.message)
            .parseMode(ParseMode.MARKDOWN)
            .withInlineKeyboard(getListMessageText(queueEntries), getListKeyboard(queueEntries))
        telegram.execute(editMessage)
    }

    override fun command() = NAME
    override fun prefix() = NAME + "_u"
    override fun scope() = Scope.USER

    companion object {
        const val NAME = "list_labs"
    }
}