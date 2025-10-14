package me.alllexey123.itmoqueue.bot.view

import me.alllexey123.itmoqueue.bot.Emoji
import me.alllexey123.itmoqueue.bot.callback.CallbackData
import me.alllexey123.itmoqueue.bot.extensions.inlineButton
import me.alllexey123.itmoqueue.bot.extensions.keyboard
import me.alllexey123.itmoqueue.bot.extensions.row
import me.alllexey123.itmoqueue.model.Group
import me.alllexey123.itmoqueue.model.Lab
import me.alllexey123.itmoqueue.model.enums.QueueType
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup

@Component
class LabQueueTypeView {


    fun getLabQueueTypeText(lab: Lab): String {
        val types = QueueType.entries.toTypedArray()

        val text = buildString {
            appendLine("Лаба *${lab.name}*")
            appendLine("Предмет: *${lab.subject.name}*")
            appendLine("Тип очереди: *${lab.queueType.ruTitle}*")
            appendLine(Emoji.DIVIDER)
            appendLine("Выберите тип очереди для этой лабы: ")
            types.forEachIndexed { i, type ->
                val pos = String.format("%2s", i + 1)
                appendLine("${pos}. *${type.ruTitle}*")
                appendLine(type.ruDescription)
            }
        }

        return text
    }

    fun getDefaultQueueTypeText(group: Group): String {
        val types = QueueType.entries.toTypedArray()

        val text = buildString {
            appendLine("Тип очереди: *${group.settings.defaultQueueType.ruTitle}*")
            appendLine(Emoji.DIVIDER)
            appendLine("Выберите тип очереди лабы по умолчанию: ")
            types.forEachIndexed { i, type ->
                val pos = String.format("%2s", i + 1)
                appendLine("${pos}. *${type.ruTitle}*")
                appendLine(type.ruDescription)
            }
        }

        return text
    }

    fun getLabQueueTypeKeyboard(selectingDefault: Boolean): InlineKeyboardMarkup {
        val types = QueueType.entries.toTypedArray()
        val selectKeys = types.mapIndexed { i, type -> selectLabType(i, type, selectingDefault) }
        val keyboard = InlineKeyboardMarkup.builder()
            .keyboard(
                row(selectKeys),
                row(back(selectingDefault))
            ).build()

        return keyboard
    }

    companion object Buttons {
        val selectLabType = { i: Int, type: QueueType, selectingDefault: Boolean ->
            inlineButton(
                "${i + 1}",
                CallbackData.LabQueueTypeSelect(selectingDefault, type)
            )
        }
        val back = { selectingDefault: Boolean ->
            inlineButton(
                Emoji.BACK,
                if (selectingDefault) CallbackData.SettingsMenu() else CallbackData.LabEditMenu()
            )
        }
    }
}