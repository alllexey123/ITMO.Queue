package dev.alllexey.itmoqueue.bot.view

import dev.alllexey.itmoqueue.bot.Emoji
import dev.alllexey.itmoqueue.bot.callback.CallbackData
import dev.alllexey.itmoqueue.bot.extensions.inlineButton
import dev.alllexey.itmoqueue.bot.extensions.keyboard
import dev.alllexey.itmoqueue.bot.extensions.row
import dev.alllexey.itmoqueue.model.Group
import dev.alllexey.itmoqueue.model.Lab
import dev.alllexey.itmoqueue.model.enums.QueueType
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