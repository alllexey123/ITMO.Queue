package dev.alllexey.itmoqueue.bot.view

import dev.alllexey.itmoqueue.bot.Emoji
import dev.alllexey.itmoqueue.bot.callback.CallbackData
import dev.alllexey.itmoqueue.bot.extensions.inlineButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow
import java.time.LocalTime
import java.time.ZoneId

object ViewUtils {

    val stPetersburgZoneId = ZoneId.of("Europe/Moscow")
    fun timeAtStPetersburg(): LocalTime {
        return LocalTime.now(stPetersburgZoneId)
    }

    fun addPagination(
        rows: MutableList<InlineKeyboardRow>,
        onClick: (page: Int) -> CallbackData,
        page: Int, // 0-indexed
        perPage: Int,
        totalEntities: Int
    ) {
        val pagination = mutableListOf<InlineKeyboardButton>()
        if (page > 0) {
            pagination.add(inlineButton(Emoji.ARROW_LEFT, onClick(page - 1)))
        }
        if ((page + 1) * perPage < totalEntities) {
            pagination.add(inlineButton(Emoji.ARROW_RIGHT, onClick(page + 1)))
        }

        if (pagination.isNotEmpty()) {
            rows.add(InlineKeyboardRow(pagination))
        }
    }

    // everything is 0-indexed
    fun calcIndex(page: Int, perPage: Int, row: Int, perRow: Int, pos: Int): Int {
        return page * perPage + row * perRow + pos + 1
    }

    // everything is 0-indexed
    fun calcIndex(page: Int, perPage: Int, pos: Int): Int {
        return page * perPage + pos + 1
    }
}