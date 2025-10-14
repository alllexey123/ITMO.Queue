package me.alllexey123.itmoqueue.bot.view

import me.alllexey123.itmoqueue.bot.Emoji
import me.alllexey123.itmoqueue.bot.callback.CallbackData
import me.alllexey123.itmoqueue.bot.extensions.atPage
import me.alllexey123.itmoqueue.bot.extensions.inlineButton
import me.alllexey123.itmoqueue.bot.extensions.row
import me.alllexey123.itmoqueue.model.Lab
import me.alllexey123.itmoqueue.model.QueueEntry
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup

@Component
class LabListView {

    fun getLabsListGroupText(
        labs: List<Lab>,
        page: Int = 0, // 0-indexed
        perPage: Int = 9
    ): String {
        val pageLabs = labs.atPage(page, perPage)
        return buildString {
            appendLine("Список лаб в группе:")
            appendLine()
            if (pageLabs.isNotEmpty()) {
                pageLabs.forEachIndexed { pos, lab ->
                    val labIndex = ViewUtils.calcIndex(page, perPage, pos)
                    appendLine("$labIndex. ${lab.name}")
                }
            } else {
                appendLine("Пока тут пусто")
            }
        }
    }

    fun getLabsListGroupKeyboard(
        labs: List<Lab>,
        page: Int = 0, // 0-indexed
        perPage: Int = 9,
        perRow: Int = 3
    ): InlineKeyboardMarkup {
        val pageLabs = labs.atPage(page, perPage)
        val rows = pageLabs.chunked(perRow).mapIndexed { row, chunk ->
            row(
                chunk.mapIndexed { pos, lab ->
                    val labIndex = ViewUtils.calcIndex(page, perPage, row, perRow, pos)
                    select(labIndex, lab)
                }
            )
        }.toMutableList()

        ViewUtils.addPagination(rows, { i -> CallbackData.ShowLabList(i) }, page, perPage, labs.size)
        rows += row(back, newLab)
        return InlineKeyboardMarkup(rows)
    }

    fun getLabsListUserText(
        entries: List<QueueEntry>,
        page: Int = 0, // 0-indexed
        perPage: Int = 9
    ): String {
        val pageEntries = entries.atPage(page, perPage)
        return buildString {
            appendLine("Список ваших лаб:")
            appendLine()
            if (pageEntries.isNotEmpty()) {
                pageEntries.forEachIndexed { pos, entry ->
                    val labIndex = ViewUtils.calcIndex(page, perPage, pos)
                    val lab = entry.lab
                    val status = if (entry.done) Emoji.CHECK else Emoji.CANCEL
                    appendLine("`$labIndex. ${lab.name} (${lab.subject.name}) [$status]`")
                }
            } else {
                appendLine("Пока тут пусто...")
            }
        }
    }

    fun getLabsListUserKeyboard(
        entries: List<QueueEntry>,
        page: Int = 0, // 0-indexed
        perPage: Int = 9,
        perRow: Int = 3
    ): InlineKeyboardMarkup {
        val pageEntries = entries.atPage(page, perPage)
        val rows = pageEntries.chunked(perRow).mapIndexed { row, chunk ->
            row(
                chunk.mapIndexed { pos, entry ->
                    val labIndex = ViewUtils.calcIndex(page, perPage, row, perRow, pos)
                    select(labIndex, entry.lab)
                }
            )
        }.toMutableList()

        ViewUtils.addPagination(rows, { i -> CallbackData.ShowLabList(i) }, page, perPage, entries.size)
        return InlineKeyboardMarkup(rows)
    }


    companion object Buttons {
        val select = { i: Int, lab: Lab -> inlineButton(i.toString(), CallbackData.LabDetailsShow(lab.id!!)) }
        val newLab = inlineButton(Emoji.PLUS, CallbackData.NewLabSelectSubjectPage(0))
        val back = inlineButton(Emoji.BACK, CallbackData.GroupListBack())
    }
}