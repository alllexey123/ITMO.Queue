package me.alllexey123.itmoqueue.bot.view

import me.alllexey123.itmoqueue.bot.Emoji
import me.alllexey123.itmoqueue.bot.callback.CallbackData
import me.alllexey123.itmoqueue.bot.extensions.inlineButton
import me.alllexey123.itmoqueue.bot.extensions.keyboard
import me.alllexey123.itmoqueue.bot.extensions.row
import me.alllexey123.itmoqueue.model.Group
import me.alllexey123.itmoqueue.model.Subject
import me.alllexey123.itmoqueue.model.enums.MergedQueueType
import me.alllexey123.itmoqueue.model.enums.QueueType
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup

@Component
class SubjectQueueTypeView {

    fun getSubjectQueueTypeText(subject: Subject): String {
        val types = QueueType.entries.toTypedArray()

        val text = buildString {
            appendLine("Предмет: *${subject.name}*")
            appendLine("Тип очереди: *${subject.mergedQueueType.ruTitle}*")
            appendLine(Emoji.DIVIDER)
            appendLine("Выберите тип очереди для этого предмета: ")
            types.forEachIndexed { i, type ->
                val pos = String.format("%2s", i + 1)
                appendLine("${pos}. *${type.ruTitle}*")
                appendLine(type.ruDescription)
            }
        }

        return text
    }

    fun getDefaultQueueTypeText(group: Group): String {
        val types = MergedQueueType.entries.toTypedArray()

        val text = buildString {
            appendLine("Тип очереди: *${group.settings.defaultMergedQueueType.ruTitle}*")
            appendLine(Emoji.DIVIDER)
            appendLine("Выберите тип очереди предмета по умолчанию: ")
            types.forEachIndexed { i, type ->
                val pos = String.format("%2s", i + 1)
                appendLine("${pos}. *${type.ruTitle}*")
                appendLine(type.ruDescription)
            }
        }

        return text
    }

    fun getSubjectQueueTypeKeyboard(selectingDefault: Boolean): InlineKeyboardMarkup {
        val types = MergedQueueType.entries.toTypedArray()
        val selectKeys = types.mapIndexed { i, type -> selectSubjectType(i, type, selectingDefault) }
        val keyboard = InlineKeyboardMarkup.builder()
            .keyboard(
                row(selectKeys),
                row(back(selectingDefault))
            ).build()

        return keyboard
    }

    companion object Buttons {
        val selectSubjectType = { i: Int, type: MergedQueueType, selectingDefault: Boolean ->
            inlineButton(
                "${i + 1}",
                CallbackData.SubjectQueueTypeSelect(selectingDefault, type)
            )
        }
        val back = { selectingDefault: Boolean ->
            inlineButton(
                Emoji.BACK,
                if (selectingDefault) CallbackData.SettingsMenu() else CallbackData.SubjectDetailsRefresh()
            )
        }
    }
}