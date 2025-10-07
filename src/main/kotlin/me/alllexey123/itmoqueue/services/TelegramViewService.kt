package me.alllexey123.itmoqueue.services

import me.alllexey123.itmoqueue.bot.Emoji
import me.alllexey123.itmoqueue.bot.callback.CallbackData
import me.alllexey123.itmoqueue.bot.callback.CallbackDataSerializer
import me.alllexey123.itmoqueue.bot.callback.ICallbackDataSerializer
import me.alllexey123.itmoqueue.bot.command.GroupNewLabCommand
import me.alllexey123.itmoqueue.bot.command.GroupNewSubjectCommand
import me.alllexey123.itmoqueue.bot.extensions.inlineButton
import me.alllexey123.itmoqueue.bot.extensions.inlineRowButton
import me.alllexey123.itmoqueue.model.Lab
import me.alllexey123.itmoqueue.model.QueueEntry
import me.alllexey123.itmoqueue.model.Subject
import me.alllexey123.itmoqueue.model.User
import me.alllexey123.itmoqueue.model.enums.MergedQueueType
import me.alllexey123.itmoqueue.model.enums.QueueType
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private const val perPage = 9
private const val perRow = 3

@Component
class TelegramViewService(
    private val callbackDataSerializer: CallbackDataSerializer,
    private val labService: LabService,
    private val subjectService: SubjectService,
    private val queueService: QueueService
) : ICallbackDataSerializer by callbackDataSerializer {

    fun buildLabDetailsText(lab: Lab?, entries: List<QueueEntry>?): String {
        if (lab == null) return "Лаба не найдена"

        val dtf = DateTimeFormatter.ofPattern("HH:mm:ss")

        return buildString {
            appendLine("Лаба *${lab.name}*")
            appendLine("Предмет: *${lab.subject.name}*")
            appendLine("——————————————")

            if (entries.isNullOrEmpty()) {
                appendLine("Очередь пуста")
            } else {
                entries.forEachIndexed { i, entry ->
                    val pos = String.format("%2s", i + 1)
                    val user = entry.user
                    val status = if (entry.done) Emoji.CHECK else Emoji.CANCEL
                    appendLine("`$pos. $status` ${user.mention()} `[${entry.attemptNumber}]`")
                }
            }
            appendLine("——————————————")
            appendLine("Обновлено: ${dtf.format(LocalTime.now())}\n")
            appendLine("[Ссылка](${labService.getLabUrl(lab)}) на полную очередь")
        }
    }

    fun buildLabDetailsGroupKeyboard(lab: Lab?, pinned: Boolean): InlineKeyboardMarkup {
        if (lab == null) return InlineKeyboardMarkup.builder().build()

        val rows = mutableListOf(
            InlineKeyboardRow(
                listOf(
                    inlineButton(Emoji.PLUS, serialize(CallbackData.AddToQueue())),
                    inlineButton(Emoji.MINUS, serialize(CallbackData.RemoveFromQueue())),
                    inlineButton(Emoji.REFRESH, serialize(CallbackData.SelectLab(lab.id!!))),
                )
            )
        )

        if (pinned) {
            rows.add(
                InlineKeyboardRow(
                    inlineButton(Emoji.CHECK, serialize(CallbackData.MarkQueueEntryDone()))
                )
            )
        } else {
            rows.add(
                InlineKeyboardRow(
                    listOf(
                        inlineButton(Emoji.PIN, serialize(CallbackData.PinLab())),
                        inlineButton(Emoji.EDIT, serialize(CallbackData.EditLab())),
                        inlineButton(Emoji.DELETE, serialize(CallbackData.DeleteLab()))
                    )
                )
            )
            rows.add(
                InlineKeyboardRow(
                    listOf(
                        inlineButton(Emoji.BACK, serialize(CallbackData.ShowLabsList())),
                        inlineButton(Emoji.CHECK, serialize(CallbackData.MarkQueueEntryDone()))
                    )
                )
            )
            rows.add(
                inlineRowButton("Изменить тип очереди", serialize(CallbackData.LabQueueTypeAsk()))
            )
        }

        return InlineKeyboardMarkup(rows)
    }

    fun buildLabQueueTypeSelectView(lab: Lab): Pair<String, InlineKeyboardMarkup> {
        val types = QueueType.entries.toTypedArray()

        val text = buildString {
            appendLine("Лаба *${lab.name}*")
            appendLine("Предмет: *${lab.subject.name}*")
            appendLine("——————————————")
            appendLine("Выберите один из представленных ниже типов очереди: ")
            types.forEachIndexed { i, type ->
                val pos = String.format("%2s", i + 1)
                appendLine("*${pos}. ${type.ruTitle}*")
                appendLine(type.ruDescription)
            }
        }

        val selectKeys =
            types.mapIndexed { i, type -> inlineButton("${i + 1}", serialize(CallbackData.LabQueueTypeSelect(type))) }

        val keyboard = InlineKeyboardMarkup.builder()
            .keyboard(
                listOf(
                    InlineKeyboardRow(selectKeys),
                    inlineRowButton(Emoji.BACK, serialize(CallbackData.SelectLab(lab.id!!))),
                )
            ).build()

        return text to keyboard
    }

    fun buildSubjectQueueTypeSelectView(subject: Subject): Pair<String, InlineKeyboardMarkup> {
        val types = MergedQueueType.entries.toTypedArray()

        val text = buildString {
            appendLine("Предмет: *${subject.name}*")
            appendLine("Тип очереди: *${subject.mergedQueueType?.ruTitle ?: "отключена"}*")
            appendLine("——————————————")
            appendLine("Выберите один из представленных ниже типов очереди: ")
            types.forEachIndexed { i, type ->
                val pos = String.format("%s", i + 1)
                appendLine("*${pos}. ${type.ruTitle}*")
                appendLine(type.ruDescription)
            }
        }

        val selectKeys =
            types.mapIndexed { i, type -> inlineButton("${i + 1}", serialize(CallbackData.MergedQueueTypeSelect(type))) }

        val keyboard = InlineKeyboardMarkup.builder()
            .keyboard(
                listOf(
                    InlineKeyboardRow(selectKeys),
                    inlineRowButton("Отключить очередь", serialize(CallbackData.MergedQueueTypeSelect(null))),
                    inlineRowButton(Emoji.BACK, serialize(CallbackData.SelectSubject(subject.id!!))),
                )
            ).build()

        return text to keyboard
    }

    fun buildLabDetailUserKeyboard(lab: Lab?): InlineKeyboardMarkup {
        if (lab == null) return InlineKeyboardMarkup.builder().build()

        val rows = mutableListOf(
            InlineKeyboardRow(
                listOf(
                    inlineButton(Emoji.PLUS, serialize(CallbackData.AddToQueue())),
                    inlineButton(Emoji.MINUS, serialize(CallbackData.RemoveFromQueue())),
                    inlineButton(Emoji.REFRESH, serialize(CallbackData.SelectLab(lab.id!!))),
                )
            ),
            InlineKeyboardRow(
                listOf(
                    inlineButton(Emoji.BACK, serialize(CallbackData.ShowLabsList())),
                    inlineButton(Emoji.CHECK, serialize(CallbackData.MarkQueueEntryDone())),
                )
            )
        )

        return InlineKeyboardMarkup(rows)
    }


    fun buildLabsListGroupText(labs: List<Lab>, page: Int = 1): String {
        val pageLabs = labs.drop((page - 1) * perPage).take(perPage)
        return buildString {
            appendLine("Список лаб в этой группе:\n")
            if (pageLabs.isNotEmpty()) {
                pageLabs.forEachIndexed { i, lab ->
                    val labIndex = (page - 1) * perPage + i + 1
                    appendLine("$labIndex. ${lab.name}")
                }
            } else {
                appendLine("Пока тут пусто")
            }
            appendLine("\nДобавить лабу - /${GroupNewLabCommand.NAME}")
        }
    }

    fun buildLabsListGroupKeyboard(labs: List<Lab>, page: Int = 1): InlineKeyboardMarkup {
        val pageLabs = labs.drop((page - 1) * perPage).take(perPage)
        val rows = pageLabs.chunked(perRow).mapIndexed { i, chunk ->
            InlineKeyboardRow(
                chunk.mapIndexed { j, lab ->
                    val labIndex = (page - 1) * perPage + i * perRow + j + 1
                    inlineButton(labIndex.toString(), serialize(CallbackData.SelectLab(lab.id!!)))
                }
            )
        }.toMutableList()

        addLabsListPagination(rows, page, labs.size)
        return InlineKeyboardMarkup(rows)
    }

    fun buildLabAttemptText(user: User, isPrivate: Boolean): String {
        return if (isPrivate) "${user.mention()}, какая это попытка?"
        else "${user.mention()}, какая это попытка?\n\n_У вас 1 минута на выбор, потом сообщение могут удалить_"
    }

    fun buildLabAttemptKeyboard(): InlineKeyboardMarkup {
        val count = 4
        val buttons = (1..count).map { i ->
            val text = if (i == count) "$i+" else "$i"
            inlineButton(text, serialize(CallbackData.SelectAttemptNumber(i)))
        } + inlineButton(Emoji.CANCEL, serialize(CallbackData.AddToQueueCancel()))

        return InlineKeyboardMarkup(listOf(InlineKeyboardRow(buttons)))
    }

    fun buildLabsListUserText(entries: List<QueueEntry>, page: Int = 1): String {
        val pageEntries = entries.drop((page - 1) * perPage).take(perPage)
        return buildString {
            appendLine("Список ваших лаб:\n")
            if (pageEntries.isNotEmpty()) {
                pageEntries.forEachIndexed { i, entry ->
                    val labIndex = (page - 1) * perPage + i + 1
                    val lab = entry.lab
                    val status = if (entry.done) Emoji.CHECK else Emoji.CANCEL
                    appendLine("`$labIndex. ${lab.name} (${lab.subject.name}) [$status]`")
                }
            } else {
                appendLine("Пока тут пусто...\n")
            }
        }
    }

    fun buildLabsListUserKeyboard(entries: List<QueueEntry>, page: Int = 1): InlineKeyboardMarkup {
        val pageEntries = entries.drop((page - 1) * perPage).take(perPage)
        val rows = pageEntries.chunked(perRow).mapIndexed { i, chunk ->
            InlineKeyboardRow(
                chunk.mapIndexed { j, entry ->
                    val labIndex = (page - 1) * perPage + i * perRow + j + 1
                    inlineButton(labIndex.toString(), serialize(CallbackData.SelectLab(entry.lab.id!!)))
                }
            )
        }.toMutableList()
        addLabsListPagination(rows, page, entries.size)

        return InlineKeyboardMarkup(rows)
    }

    private fun addLabsListPagination(rows: MutableList<InlineKeyboardRow>, page: Int, totalEntities: Int) {
        val pagination = mutableListOf<InlineKeyboardButton>()
        if (page > 1) {
            pagination.add(inlineButton(Emoji.ARROW_LEFT, serialize(CallbackData.ShowLabsPage(page - 1))))
        }
        if (page * perPage < totalEntities) {
            pagination.add(inlineButton(Emoji.ARROW_RIGHT, serialize(CallbackData.ShowLabsPage(page + 1))))
        }

        if (pagination.isNotEmpty()) {
            rows.add(InlineKeyboardRow(pagination))
        }
    }

    fun buildSubjectsListText(subjects: List<Subject>, page: Int = 1): String {
        val pageEntries = subjects.drop((page - 1) * perPage).take(perPage)
        return buildString {
            if (subjects.isNotEmpty()) {
                appendLine("Список предметов:\n")
                pageEntries.forEachIndexed { i, subject ->
                    val subjectIndex = (page - 1) * perPage + i + 1
                    appendLine("${subjectIndex}. ${subject.name}")
                }
                appendLine()
            } else {
                appendLine("Пока тут пусто\n")
            }
            appendLine("Добавить предмет - /${GroupNewSubjectCommand.NAME}")
        }
    }

    fun buildSubjectsListKeyboard(subjects: List<Subject>, page: Int = 1): InlineKeyboardMarkup {
        val pageEntries = subjects.drop((page - 1) * perPage).take(perPage)
        val rows = pageEntries.chunked(perRow).mapIndexed { i, chunk ->
            InlineKeyboardRow(
                chunk.mapIndexed { j, subject ->
                    val subjectIndex = (page - 1) * perPage + i * perRow + j + 1
                    inlineButton(
                        subjectIndex.toString(),
                        serialize(CallbackData.SelectSubject(subject.id!!))
                    )
                }
            )
        }.toMutableList()

        addSubjectsListPagination(rows, page, subjects.size)
        return InlineKeyboardMarkup(rows)
    }

    private fun addSubjectsListPagination(rows: MutableList<InlineKeyboardRow>, page: Int, totalEntities: Int) {
        val pagination = mutableListOf<InlineKeyboardButton>()
        if (page > 1) {
            pagination.add(inlineButton(Emoji.ARROW_LEFT, serialize(CallbackData.ShowSubjectsPage(page - 1))))
        }
        if (page * perPage < totalEntities) {
            pagination.add(inlineButton(Emoji.ARROW_RIGHT, serialize(CallbackData.ShowSubjectsPage(page + 1))))
        }

        if (pagination.isNotEmpty()) {
            rows.add(InlineKeyboardRow(pagination))
        }
    }

    fun buildSubjectDetailsText(subject: Subject?): String {
        if (subject == null) {
            return "Предмет не найден"
        }

        val dtf = DateTimeFormatter.ofPattern("HH:mm:ss")

        return buildString {
            appendLine("Предмет: *${subject.name}*")
            if (subject.isMergedQueueEnabled()) {
                appendLine("Тип очереди: *${subject.mergedQueueType!!.ruTitle}*")
                val entries = queueService.sortedEntries(subject).filter { !it.done }
                val limited = entries.take(15)
                val labs = limited.map { it.lab }.distinct()
                appendLine("——————————————")
                labs.forEachIndexed { i, lab ->
                    val pos = String.format("%2s", i + 1)
                    appendLine("${pos}. ${lab.name}")
                }
                appendLine("——————————————")


                if (limited.isEmpty()) {
                    appendLine("Очередь пуста")
                } else {
                    limited.forEachIndexed { i, entry ->
                        val pos = String.format("%2s", i + 1)
                        val labPos = String.format("%2s", labs.indexOf(entry.lab))
                        val user = entry.user
                        val status = if (entry.done) Emoji.CHECK else Emoji.CANCEL
                        appendLine("`$pos. `|${labPos}| $status` ${user.mention()} `[${entry.attemptNumber}]`")
                    }
                    if (entries.size > limited.size) {
                        print("_... и ещё ${entries.size - limited.size}_")
                    }
                }
                appendLine("——————————————")
                appendLine("Обновлено: ${dtf.format(LocalTime.now())}\n")
                appendLine("[Ссылка](${subjectService.getSubjectUrl(subject)}) на полную очередь")
            } else {
                appendLine("*Очередь отключена*")
            }
        }
    }

    fun buildSubjectDetailsKeyboard(subject: Subject?): InlineKeyboardMarkup {
        if (subject == null) {
            return InlineKeyboardMarkup.builder().build()
        }

        val rows = mutableListOf<InlineKeyboardRow>()
        rows.add(
            InlineKeyboardRow(
                inlineButton(Emoji.BACK, serialize(CallbackData.ShowSubjectsList())),
                inlineButton(Emoji.EDIT, serialize(CallbackData.EditSubject())),
                inlineButton(Emoji.DELETE, serialize(CallbackData.DeleteSubject()))
            )
        )

        rows.add(
            InlineKeyboardRow(
                inlineButton("Настроить очередь", serialize(CallbackData.MergedQueueTypeAsk())),
            )
        )

        rows.add(
            InlineKeyboardRow(
                inlineButton("Лабы", serialize(CallbackData.ShowSubjectLabsList())),
            )
        )

        return InlineKeyboardMarkup.builder().keyboard(rows).build()
    }


    fun buildSubjectLabListText(subject: Subject?): String {
        if (subject == null) {
            return "Предмет не найден"
        }

        return buildString {
            appendLine("Предмет: *${subject.name}*")
            if (subject.labs.isEmpty()) {
                appendLine("Лабораторных работ пока не было")
            } else {
                appendLine("Лабы: ")
                subject.labs.forEachIndexed { i, labWork ->
                    val pos = String.format("%2s", i + 1)
                    appendLine("$pos. ${labWork.name}")
                }
            }
        }
    }

    fun buildSubjectLabListKeyboard(subject: Subject?): InlineKeyboardMarkup {
        if (subject == null) {
            return InlineKeyboardMarkup.builder().build()
        }

        val rows = mutableListOf<InlineKeyboardRow>()
        rows.add(
            InlineKeyboardRow(
                listOf(
                    inlineButton(Emoji.BACK, serialize(CallbackData.SelectSubject(subject.id!!))),
                )
            )
        )

        return InlineKeyboardMarkup.builder().keyboard(rows).build()
    }
}