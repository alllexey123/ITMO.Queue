package me.alllexey123.itmoqueue.bot.view

import me.alllexey123.itmoqueue.bot.Emoji
import me.alllexey123.itmoqueue.bot.callback.CallbackData
import me.alllexey123.itmoqueue.bot.extensions.inlineButton
import me.alllexey123.itmoqueue.bot.extensions.row
import me.alllexey123.itmoqueue.model.QueueEntry
import me.alllexey123.itmoqueue.model.Subject
import me.alllexey123.itmoqueue.services.SubjectService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Component
class SubjectView(private val subjectService: SubjectService) {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    fun getSubjectText(subject: Subject, entries: List<QueueEntry>, hideAttempts: Boolean = false): String {
        return buildString {
            appendLine("Предмет *${subject.name}*")
            appendLine("——————————————")
            val limited = entries.take(15)
            val labs = limited.map { it.lab }.distinct()

            if (!labs.isEmpty()) {
                appendLine("Обозначения лаб:")
                labs.forEachIndexed { i, lab ->
                    val pos = String.format("%2s", i + 1)
                    appendLine("${pos}. ${lab.name}")
                }
                appendLine("——————————————")
            }

            if (limited.isEmpty()) {
                appendLine("Очередь пуста")
            } else {
                limited.forEachIndexed { i, entry ->
                    val pos = String.format("%2s", i + 1)
                    val labPos = labs.indexOf(entry.lab) + 1
                    val user = entry.user
                    val status = if (entry.done) Emoji.CHECK else Emoji.CANCEL
                    append("`$pos. |$labPos| $status` ${user.mention()}")
                    appendLine(if (!hideAttempts) " `[${entry.attemptNumber}]`" else "")
                }
                if (entries.size > limited.size) {
                    print("_... и ещё ${entries.size - limited.size}_")
                }
            }
            appendLine("——————————————")
            appendLine("Обновлено: ${timeFormatter.format(LocalTime.now())}\n")
            appendLine("[Ссылка](${subjectService.getSubjectUrl(subject)}) на полную очередь")
        }
    }

    fun getSubjectKeyboard(): InlineKeyboardMarkup {
        val rows = mutableListOf(
            row(showLabList, edit, refresh),
            row(showSubjectList, deleteAsk)
        )

        return InlineKeyboardMarkup(rows)
    }

    fun getSubjectLabListText(subject: Subject): String {
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

    fun getSubjectLabListKeyboard(): InlineKeyboardMarkup {
        return InlineKeyboardMarkup(listOf(row(backToSubjectDetails)))
    }

    companion object Buttons {
        val refresh = inlineButton(Emoji.REFRESH, CallbackData.SubjectDetailsRefresh())
        val deleteAsk = inlineButton(Emoji.DELETE, CallbackData.SubjectDeleteAsk())
        val edit = inlineButton(Emoji.GEAR, CallbackData.SubjectEditMenu())
        val showSubjectList = inlineButton(Emoji.BACK, CallbackData.ShowSubjectList())
        val backToSubjectDetails = inlineButton(Emoji.BACK, CallbackData.SubjectDetailsRefresh())
        val showLabList = inlineButton("Лабы", CallbackData.SubjectShowLabList())
    }
}