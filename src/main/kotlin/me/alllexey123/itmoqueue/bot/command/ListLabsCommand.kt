package me.alllexey123.itmoqueue.bot.command

import me.alllexey123.itmoqueue.bot.Emoji
import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.bot.callback.CallbackHandler
import me.alllexey123.itmoqueue.bot.extensions.edit
import me.alllexey123.itmoqueue.bot.extensions.inlineButton
import me.alllexey123.itmoqueue.bot.extensions.inlineRowButton
import me.alllexey123.itmoqueue.bot.extensions.withInlineKeyboard
import me.alllexey123.itmoqueue.bot.state.EditLabNameState
import me.alllexey123.itmoqueue.bot.state.StateManager
import me.alllexey123.itmoqueue.model.LabWork
import me.alllexey123.itmoqueue.model.Queue
import me.alllexey123.itmoqueue.model.QueueEntry
import me.alllexey123.itmoqueue.model.QueueType
import me.alllexey123.itmoqueue.services.*
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.stream.IntStream

@Component
class ListLabsCommand(
    private val groupService: GroupService,
    private val telegram: Telegram,
    private val stateManager: StateManager,
    private val labWorkService: LabWorkService,
    private val editLabNameState: EditLabNameState,
    private val queueService: QueueService,
    private val userService: UserService
) :
    CommandHandler, CallbackHandler {

    override fun handle(message: Message) {
        val chat = message.chat
        val group = groupService.getOrCreateByChatId(chat.id)
        val labs = group.labs
        val sendMessage = SendMessage.builder()
            .chatId(message.chat.id)
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

        val rows = pageLabs.chunked(perRow).map { chunk ->
            InlineKeyboardRow(
                chunk.mapIndexed { i, lab ->
                    val labIndex = ((page - 1) * perPage + i + 1).toString()
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

        var queue = lab.queues.getOrNull(0)

        if (queue == null) {
            queue = queueService.save(
                Queue(
                    labWork = lab,
                    type = QueueType.FIRST_PRIORITY,
                    teacher = null
                )
            )

            queueService.save(queue)
        }

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
                    appendLine("${i + 1}. [${user.nickname}](tg://user?id=${user.id}) ")
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

    override fun handle(callbackQuery: CallbackQuery) {
        val data = decode(callbackQuery.data)
        val message = callbackQuery.message
        val chat = message.chat
        val from = callbackQuery.from
        when (data[0]) {
            "select" -> {
                val lab = labWorkService.findById(data[1].toLong())
                val lastUpdate = data.getOrNull(2)?.let { str ->
                    return@let LocalDateTime.parse(str)
                }
                val from = LocalDateTime.now().minusSeconds(15)
                if (lastUpdate != null && lastUpdate > from) {
                    val sendToast = AnswerCallbackQuery.builder()
                        .callbackQueryId(callbackQuery.id)
                        .text("Не так быстро! Обновлять можно раз в 15 секунд.")
                        .cacheTime(from.until(from, ChronoUnit.SECONDS).toInt())
                        .build()

                    telegram.execute(sendToast)
                } else {
                    val editMessage = EditMessageText.builder()
                        .edit(message)
                        .parseMode(ParseMode.MARKDOWN)
                        .withInlineKeyboard(getLabText(lab), getLabKeyboard(lab))

                    telegram.execute(editMessage)
                }
            }

            "delete" -> {
                labWorkService.deleteById(data[1].toLong())
                labWorkService.flush()

                updateLabsList(message)
            }

            "edit" -> {
                val labId = data[1].toLong()
                val editMessage = EditMessageText.builder()
                    .edit(message)
                    .text("Введите новое название лабы (ответом на это сообщение):")
                    .replyMarkup(
                        InlineKeyboardMarkup.builder().keyboardRow(
                            inlineRowButton(Emoji.BACK, encode("select", labId)),
                        ).build()
                    )
                    .build()

                editLabNameState.setChatData(chat.id, labId)
                stateManager.setHandler(chat.id, editLabNameState)

                telegram.execute(editMessage)
            }

            "main" -> {
                updateLabsList(message)
            }

            "lab_page" -> {
                val page = data[1].toInt()
                val labs = groupService.getOrCreateByChatId(chat.id).labs

                val editMessage = EditMessageText.builder()
                    .edit(message)
                    .withInlineKeyboard(getListMessageText(labs, page), getListKeyboard(labs, page = page))

                telegram.execute(editMessage)
            }

            "add_to_queue" -> {
                val lab = labWorkService.findById(data[1].toLong())
                val queue = lab!!.queues[0]
                val user = userService.getOrCreateByTelegramId(from.id, from.userName)

                if (queue.entries.filter { entry -> !entry.done }.any { it.user == user }) {
                    val action = AnswerCallbackQuery.builder()
                        .text("Вы уже присутствуете в очереди!")
                        .cacheTime(10)
                        .callbackQueryId(callbackQuery.id)
                        .build()

                    telegram.execute(action)
                } else {
                    val action = SendMessage.builder()
                        .chatId(chat.id)
                        .text("Какая это попытка?")
                        .replyMarkup(getAttemptKeyboard(lab.id))
                        .build()

                    telegram.execute(action)
                }
            }

            "remove_from_queue" -> {
                val lab = labWorkService.findById(data[1].toLong())
                val queue = lab!!.queues[0]
                val user = userService.getOrCreateByTelegramId(from.id, from.userName)

            }

            "add_to_queue_attempt" -> {
                val lab = labWorkService.findById(data[1].toLong())
                val attempt = data[2].toInt()
                val user = userService.getOrCreateByTelegramId(from.id, from.userName)
                val queue = lab!!.queues[0]

                val editMessage = EditMessageText.builder()
                    .edit(message)
                if (queue.entries.filter { entry -> !entry.done }.any { it.user == user }) {
                    editMessage.text("Вы уже присутствуете в очереди")
                } else {
                    queueService.save(
                        QueueEntry(
                            user = user,
                            queue = queue,
                            attemptNumber = attempt,
                            addedAt = OffsetDateTime.now()
                        )
                    )

                    editMessage.text("Вы добавлены в очередь")
                }

                telegram.execute(editMessage.build())
            }
        }
    }

    fun updateLabsList(message: MaybeInaccessibleMessage) {
        val labs = groupService.getOrCreateByChatId(message.chatId).labs
        val editMessage = EditMessageText.builder()
            .edit(message)
            .withInlineKeyboard(getListMessageText(labs), getListKeyboard(labs))
        telegram.execute(editMessage)
    }


    override fun prefix() = command()

    override fun command() = "list_labs"

    override fun scope() = Scope.GROUP
}