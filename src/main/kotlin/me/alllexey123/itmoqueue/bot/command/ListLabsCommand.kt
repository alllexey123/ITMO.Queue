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
import me.alllexey123.itmoqueue.bot.state.EditLabNameState
import me.alllexey123.itmoqueue.bot.state.StateManager
import me.alllexey123.itmoqueue.model.LabWork
import me.alllexey123.itmoqueue.services.LabWorkService
import me.alllexey123.itmoqueue.services.QueueService
import me.alllexey123.itmoqueue.services.Telegram
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.stream.IntStream

private const val secondsPerRefresh: Long = 3

@Component
class ListLabsCommand(
    private val telegram: Telegram,
    private val stateManager: StateManager,
    private val labWorkService: LabWorkService,
    private val editLabNameState: EditLabNameState,
    private val queueService: QueueService,
) : CommandHandler, CallbackHandler {

    override fun handle(context: MessageContext) {
        val group = context.group!!
        val labs = group.labs
        val sendMessage = context.send()
            .withInlineKeyboard(getListMessageText(labs), getListKeyboard(labs))

        telegram.execute(sendMessage)
    }

    val perPage = 9

    fun getListMessageText(labs: List<LabWork>, page: Int = 1): String {
        val pageLabs = labs.drop((page - 1) * perPage).take(perPage)

        return buildString {
            if (pageLabs.isNotEmpty()) {
                appendLine("Список лаб:\n")
                pageLabs.forEachIndexed { i, lab ->
                    val labIndex = ((page - 1) * perPage + i + 1).toString()
                    appendLine("${labIndex}. ${lab.name}")
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

        val rows = pageLabs.chunked(perRow).mapIndexed { i, chunk ->
            InlineKeyboardRow(
                chunk.mapIndexed { j, lab ->
                    val labIndex = ((page - 1) * perPage + i * perRow + j + 1).toString()
                    inlineButton(labIndex, encode("select", lab.id))
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


    fun getLabText(lab: LabWork?): String {
        if (lab == null) {
            return "Лаба не найдена"
        }

        val queue = lab.queues.first()

        val validEntries = queueService.sortedEntries(queue).filter { entry ->
            !entry.done
        }

        return buildString {
            appendLine("Лаба *${lab.name}*")
            appendLine("Предмет: *${lab.subject.name}*")
            val dtf = DateTimeFormatter.ofPattern("HH:mm:ss")
            appendLine("Обновлено: " + dtf.format(LocalTime.now()))
            appendLine()
            if (validEntries.isEmpty()) {
                appendLine("Очередь пуста")
            } else {
                appendLine("Очередь:")
                validEntries.forEachIndexed { i, entry ->
                    val user = entry.user
                    appendLine("${i + 1}. \\[${entry.attemptNumber}] ${user.mention()}")
                }
            }
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
                    inlineButton(Emoji.PLUS, encode("add_to_queue", lab.id)),
                    inlineButton(Emoji.MINUS, encode("remove_from_queue", lab.id)),
                    inlineButton(Emoji.REFRESH, encode("select", lab.id, LocalDateTime.now())),
                )
            )
        )
        rows.add(
            InlineKeyboardRow(
                listOf(
                    inlineButton(Emoji.BACK, encode("main")),
                    inlineButton(Emoji.EDIT, encode("edit", lab.id)),
                    inlineButton(Emoji.DELETE, encode("delete", lab.id))
                )
            )
        )

        return InlineKeyboardMarkup.builder().keyboard(rows).build()
    }

    fun getAttemptKeyboard(labId: Long?): InlineKeyboardMarkup {
        val count = 4
        return InlineKeyboardMarkup.builder().keyboardRow(
            InlineKeyboardRow(
                IntStream.range(1, count + 1)
                    .mapToObj { i ->
                        inlineButton(if (i == count) "$i+" else "$i", encode("add_to_queue_attempt", labId, i))
                    }.toList()
            )
        ).build()
    }

    override fun handle(context: CallbackContext) {
        when (context.asString(0)) {
            "select" -> handleLabSelectQuery(context)

            "delete" -> handleLabDeleteQuery(context)

            "edit" -> handleLabEditQuery(context)

            "main" -> updateLabsList(context)

            "lab_page" -> handleLabPageQuery(context)

            "add_to_queue" -> handleLabAddToQueueQuery(context)

            "remove_from_queue" -> handleRemoveFromQueueQuery(context)

            "add_to_queue_attempt" -> handleAddToQueueAttemptQuery(context)
        }
    }

    fun handleRemoveFromQueueQuery(context: CallbackContext) {
        val lab = labWorkService.findById(context.asLong(1))
        val queue = lab!!.queues[0]

        val answer = AnswerCallbackQuery.builder().callbackQueryId(context.id)
        if (queueService.removeUserFromQueue(context.user, queue)) {
            answer.text("Вы успешно удалены из очереди")
        } else {
            answer.text("Вас нет в этой очереди")
        }

        telegram.execute(answer.build())
    }

    fun handleAddToQueueAttemptQuery(context: CallbackContext) {
        val lab = labWorkService.findById(context.asLong(1))
        val attempt = context.asInt(2)
        val queue = lab!!.queues[0]

        val action = AnswerCallbackQuery.builder()
            .callbackQueryId(context.id)
        if (queueService.isActiveInQueue(context.user, queue)) {
            action.text("Вы уже присутствуете в очереди")
        } else {
            queueService.addToQueue(context.user, queue, attempt)
            action.text("Вы добавлены в очередь")
        }

        val deleteMessage = DeleteMessage.builder()
            .chatId(context.chatId)
            .messageId(context.messageId)

        telegram.execute(action.build())
        telegram.execute(deleteMessage.build())
    }

    fun handleLabAddToQueueQuery(context: CallbackContext) {
        val lab = labWorkService.findById(context.asLong(1))
        val queue = lab!!.queues[0]

        if (queueService.isActiveInQueue(context.user, queue)) {
            val action = AnswerCallbackQuery.builder()
                .text("Вы уже присутствуете в очереди!")
                .cacheTime(10)
                .callbackQueryId(context.id)
                .build()

            telegram.execute(action)
        } else {
            val action = SendMessage.builder()
                .chatId(context.chatId)
                .parseMode(ParseMode.MARKDOWN)
                .text("${context.user.mention()}, какая это попытка?")
                .replyMarkup(getAttemptKeyboard(lab.id))
                .build()

            telegram.execute(action)
        }
    }

    fun handleLabPageQuery(context: CallbackContext) {
        val page = context.asInt(1)
        val labs = context.group!!.labs

        val editMessage = EditMessageText.builder()
            .edit(context.message)
            .withInlineKeyboard(getListMessageText(labs, page), getListKeyboard(labs, page = page))

        telegram.execute(editMessage)
    }

    fun handleLabEditQuery(context: CallbackContext) {
        val labId = context.asLong(1)
        val editMessage = EditMessageText.builder()
            .edit(context.message)
            .text("Введите новое название лабы (ответом на это сообщение):")
            .replyMarkup(
                InlineKeyboardMarkup.builder().keyboardRow(
                    inlineRowButton(Emoji.BACK, encode("select", labId)),
                ).build()
            )
            .build()

        editLabNameState.setChatData(context.chatId, labId)
        stateManager.setHandler(context.chatId, editLabNameState)

        telegram.execute(editMessage)
    }

    fun handleLabDeleteQuery(context: CallbackContext) {
        labWorkService.deleteById(context.asLong(1))
        updateLabsList(context)
    }

    fun handleLabSelectQuery(context: CallbackContext) {
        fun extracted() {
            val lab = labWorkService.findById(context.asLong(1))
            val lastUpdate = context.data.getOrNull(2)?.let { str ->
                return@let LocalDateTime.parse(str)
            }
            val from = LocalDateTime.now().minusSeconds(secondsPerRefresh)
            if (lastUpdate != null && lastUpdate > from) {
                val sendToast = AnswerCallbackQuery.builder()
                    .callbackQueryId(context.id)
                    .text("Не так быстро! Обновлять можно раз в $secondsPerRefresh секунд.")
                    .cacheTime(from.until(from, ChronoUnit.SECONDS).toInt())
                    .build()

                telegram.execute(sendToast)
            } else {
                val editMessage = EditMessageText.builder()
                    .edit(context.message)
                    .parseMode(ParseMode.MARKDOWN)
                    .withInlineKeyboard(getLabText(lab), getLabKeyboard(lab))

                telegram.execute(editMessage)
            }
        }

        extracted()
    }

    fun updateLabsList(context: CallbackContext) {
        val labs = context.group!!.labs
        val editMessage = EditMessageText.builder()
            .edit(context.message)
            .withInlineKeyboard(getListMessageText(labs), getListKeyboard(labs))
        telegram.execute(editMessage)
    }


    override fun prefix() = command()

    override fun command() = "list_labs"

    override fun scope() = Scope.GROUP
}