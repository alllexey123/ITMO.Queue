package me.alllexey123.itmoqueue.bot.view

import me.alllexey123.itmoqueue.bot.Emoji
import me.alllexey123.itmoqueue.bot.callback.CallbackData
import me.alllexey123.itmoqueue.bot.extensions.inlineButton
import me.alllexey123.itmoqueue.bot.extensions.row
import me.alllexey123.itmoqueue.model.Group
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow

@Component
class GroupSettingsView {

    fun getGroupSettingsText(group: Group): String {
        val settings = group.settings!!
        return buildString {
            appendLine("Группа *${group.name}*")
            appendLine(Emoji.DIVIDER)
            appendLine("• Попытки: ${if (settings.attemptsEnabled) Emoji.CHECK else Emoji.CANCEL}")
            if (settings.attemptsEnabled) {
                appendLine("• Попытки в ЛС: ${if (settings.askAttemptsDirectly) Emoji.CHECK else Emoji.CANCEL}")
                appendLine("• Тип очереди лабы: ${settings.defaultQueueType.ruTitle}")
                appendLine("• Тип очереди предмета: ${settings.defaultMergedQueueType.ruTitle}")
            }
            appendLine("• Топик по умолчанию: ${if (settings.forceSpecificThread()) "*установлен*" else "*не установлен*"}")
        }
    }

    fun getGroupSettingsKeyboard(group: Group, showThreadId: Boolean): InlineKeyboardMarkup {
        val settings = group.settings!!
        val rows: MutableList<InlineKeyboardRow> = mutableListOf()
        rows.add(row(switchSetting(SwitchSetting.ATTEMPTS_ENABLED, settings.attemptsEnabled)))
        if (settings.attemptsEnabled) {
            rows.add(row(switchSetting(SwitchSetting.ASK_ATTEMPTS_DIRECTLY, settings.askAttemptsDirectly)))
            rows.add(row(labDefaultQueueType))
            rows.add(row(subjectDefaultQueueType))
        }
        if (showThreadId) rows.add(row(selectThread))
        if (settings.forceSpecificThread()) rows.add(row(resetThread))
        rows.add(row(back))
        return InlineKeyboardMarkup(rows)
    }

    companion object Buttons {
        val switchSetting = { s: SwitchSetting, isEnabled: Boolean ->
            inlineButton(
                if (isEnabled) s.disableText else s.enableText,
                CallbackData.SettingsSwitchSetting(s, !isEnabled)
            )
        }

        val labDefaultQueueType = inlineButton("Изменить тип очереди лабы", CallbackData.LabQueueTypeAsk(true))
        val subjectDefaultQueueType = inlineButton("Изменить тип очереди предмета", CallbackData.SubjectQueueTypeAsk(true))
        val selectThread = inlineButton("Выбрать топик бота", CallbackData.SettingsSelectThread())
        val resetThread = inlineButton("Сбросить топик бота", CallbackData.SettingsResetThread())
        val back = inlineButton(Emoji.BACK, CallbackData.GroupListBack())
    }

    enum class SwitchSetting(val enableText: String, val disableText: String) {
        ATTEMPTS_ENABLED("Вкл попытки", "Выкл попытки"),
        ASK_ATTEMPTS_DIRECTLY("Вкл попытки в ЛС", "Выкл попытки в ЛС")
    }
}