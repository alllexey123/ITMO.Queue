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
class NewLabView {

    fun getNewLabSelectSubjectText(): String {
        return "Выберите предмет: "
    }

    fun getNewLabNoSubjectsText(): String {
        return "Не добавлен ни один предмет! Добавьте предмет нажав кнопку ниже."
    }

    fun getNewLabSelectSubjectKeyboard(
        subjects: List<Subject>,
        page: Int = 0, // 0-indexed
        perPage: Int = 5
    ): InlineKeyboardMarkup {
        val pageSubjects = subjects.atPage(page, perPage)
        val rows = pageSubjects.mapIndexed { pos, subject ->
            row(selectSubject(subject))
        }.toMutableList()

        ViewUtils.addPagination(rows, { i -> CallbackData.NewLabSelectSubjectPage(i) }, page, perPage, subjects.size)
        rows += row(back, createSubject)
        return InlineKeyboardMarkup(rows)
    }

    fun getNewLabEnterNameText(): String {
        return "Введите название для лабы (ответом на это сообщение)"
    }

    fun getNewLabEnterNameKeyboard(): InlineKeyboardMarkup {
        return InlineKeyboardMarkup(listOf(row(enterNameBack)))
    }


    companion object Buttons {
        val selectSubject = { s: Subject -> inlineButton(s.name, CallbackData.NewLabSelectSubject(s.id!!))}
        val enterNameBack = inlineButton(Emoji.BACK, CallbackData.NewLabSelectSubjectPage(0))
        val createSubject = inlineButton(Emoji.PLUS, CallbackData.NewSubjectMenu())
        val back = inlineButton(Emoji.BACK, CallbackData.NewMenu())
    }
}