package me.alllexey123.itmoqueue.bot.view

import me.alllexey123.itmoqueue.bot.Emoji
import me.alllexey123.itmoqueue.bot.callback.CallbackData
import me.alllexey123.itmoqueue.bot.extensions.atPage
import me.alllexey123.itmoqueue.bot.extensions.inlineButton
import me.alllexey123.itmoqueue.bot.extensions.row
import me.alllexey123.itmoqueue.model.Subject
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup

@Component
class SubjectListView {

    fun getSubjectListText(
        subjects: List<Subject>,
        page: Int = 0, // 0-indexed
        perPage: Int = 9
    ): String {
        val pageSubjects = subjects.atPage(page, perPage)
        return buildString {
            appendLine("Список предметов в группе:")
            appendLine()
            if (pageSubjects.isNotEmpty()) {
                pageSubjects.forEachIndexed { pos, subject ->
                    val subjectIndex = ViewUtils.calcIndex(page, perPage, pos)
                    appendLine("$subjectIndex. ${subject.name}")
                }
            } else {
                appendLine("Пока тут пусто")
            }
        }
    }

    fun getSubjectListKeyboard(
        subjects: List<Subject>,
        page: Int = 0, // 0-indexed
        perPage: Int = 9,
        perRow: Int = 3
    ): InlineKeyboardMarkup {
        val pageLabs = subjects.atPage(page, perPage)
        val rows = pageLabs.chunked(perRow).mapIndexed { row, chunk ->
            row(
                chunk.mapIndexed { pos, lab ->
                    val labIndex = ViewUtils.calcIndex(page, perPage, row, perRow, pos)
                    select(labIndex, lab)
                }
            )
        }.toMutableList()

        ViewUtils.addPagination(rows, { i -> CallbackData.ShowSubjectList(i) }, page, perPage, subjects.size)
        rows += row(back, newSubject)
        return InlineKeyboardMarkup(rows)
    }

    companion object Buttons {
        val select = { i: Int, subject: Subject -> inlineButton(i.toString(), CallbackData.SubjectDetailsShow(subject.id!!)) }
        val newSubject = inlineButton(Emoji.PLUS, CallbackData.NewSubjectMenu())
        val back = inlineButton(Emoji.BACK, CallbackData.GroupListBack())
    }
}