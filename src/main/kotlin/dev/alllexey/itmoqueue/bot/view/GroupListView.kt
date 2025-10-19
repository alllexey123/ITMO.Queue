package dev.alllexey.itmoqueue.bot.view

import dev.alllexey.itmoqueue.bot.Emoji
import dev.alllexey.itmoqueue.bot.callback.CallbackData
import dev.alllexey.itmoqueue.bot.extensions.inlineButton
import dev.alllexey.itmoqueue.bot.extensions.row
import dev.alllexey.itmoqueue.model.Group
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup

@Component
class GroupListView {

    fun getGroupListText(groups: List<Group>): String {
        if (groups.isEmpty()) {
            return "Вы пока не проявляли активность ни в какой группе."
        } else {
            return buildString {
                appendLine("Список ваших групп:")
                appendLine()
                groups.forEachIndexed { index, group -> appendLine("${"%2s".format(index + 1)}. ${group.name}") }
            }
        }
    }

    fun getGroupMenuText(group: Group): String {
        return buildString {
            appendLine("Группа *${group.name}*")
            appendLine("Активных участников: *${group.members.count()}*")
        }
    }

    fun getGroupListKeyboard(groups: List<Group>): InlineKeyboardMarkup {
        val rows = groups.mapIndexed { i, group -> row(select(i + 1, group)) }
        return InlineKeyboardMarkup(rows)
    }

    fun getGroupMenuKeyboard(isPrivate: Boolean = false): InlineKeyboardMarkup {
        val rows = mutableListOf(row(labList, subjectList))
        rows += if (isPrivate) row(backToList, settings) else row(settings)
        return InlineKeyboardMarkup(rows)
    }

    companion object Buttons {
        val select = { i: Int, g: Group -> inlineButton(i.toString(), CallbackData.GroupListSelect(g.id!!)) }
        val settings = inlineButton(Emoji.GEAR, CallbackData.SettingsMenu())
        val labList = inlineButton("Лабы", CallbackData.ShowLabList())
        val subjectList = inlineButton("Предметы", CallbackData.ShowSubjectList())
        val backToList = inlineButton(Emoji.BACK, CallbackData.GroupListMenu())
    }

}