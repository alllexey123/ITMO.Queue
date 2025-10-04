package me.alllexey123.itmoqueue.bot.command

import me.alllexey123.itmoqueue.bot.Emoji
import me.alllexey123.itmoqueue.bot.MessageContext
import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.bot.callback.CallbackContext
import me.alllexey123.itmoqueue.bot.callback.CallbackData
import me.alllexey123.itmoqueue.bot.callback.CallbackDataSerializer
import me.alllexey123.itmoqueue.bot.callback.CallbackHandler
import me.alllexey123.itmoqueue.bot.callback.ICallbackDataSerializer
import me.alllexey123.itmoqueue.model.Subject
import me.alllexey123.itmoqueue.services.Telegram
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow
import me.alllexey123.itmoqueue.bot.extensions.*
import me.alllexey123.itmoqueue.bot.state.EnterLabNameState
import me.alllexey123.itmoqueue.bot.state.StateManager

@Component
class GroupNewLabCommand(
    private val telegram: Telegram,
    private val stateManager: StateManager,
    private val enterLabNameState: EnterLabNameState,
    private val callbackDataSerializer: CallbackDataSerializer
) : CommandHandler, CallbackHandler, ICallbackDataSerializer by callbackDataSerializer {

    override fun handleMessage(context: MessageContext) {
        if (!context.requireAdmin(telegram)) return
        val group = context.group!!
        val send = context.sendReply()

        val subjects = group.subjects
        if (subjects.isNotEmpty()) {
            send.withTextAndInlineKeyboard("Выберите предмет: ", getSubjectListButtons(subjects))
        } else {
            send.text("Не добавлен ни один предмет\n\nДобавить предмет - /${GroupNewSubjectCommand.NAME}")
        }

        telegram.execute(send.build())
    }

    fun getSubjectListButtons(subjects: List<Subject>, page: Int = 1): InlineKeyboardMarkup {
        val perPage = 5

        val pageSubjects = subjects.drop((page - 1) * perPage).take(perPage)

        val rows = pageSubjects.map { subject ->
            inlineRowButton(subject.name, serialize(CallbackData.NewLabPickSubject(subject.id!!)))
        }.toMutableList()

        val pagination = mutableListOf<InlineKeyboardButton>()
        if (page > 1) {
            pagination.add(inlineButton(Emoji.ARROW_LEFT, serialize(CallbackData.NewLabSubjectsPage(page - 1))))
        }
        if (page * perPage < subjects.size) {
            pagination.add(inlineButton(Emoji.ARROW_RIGHT, serialize(CallbackData.NewLabSubjectsPage(page + 1))))
        }
        if (pagination.isNotEmpty()) {
            rows.add(InlineKeyboardRow(pagination))
        }

        rows.add(inlineRowButton(Emoji.CANCEL, serialize(CallbackData.NewLabCancel())))

        return InlineKeyboardMarkup(rows)
    }


    override fun handleCallback(context: CallbackContext) {
        when (val data = context.data) {
            is CallbackData.NewLabCancel -> handleCancel(context)
            is CallbackData.NewLabPickSubject -> handlePickSubject(context, data.subjectId)
            is CallbackData.NewLabSubjectsPage -> handleSubjectsPage(context, data.page)
            else -> {}
        }
    }

    fun handleCancel(context: CallbackContext) {
        telegram.execute(
            EditMessageText.builder()
                .text("Создание лабы отменено")
                .edit(context.message)
                .build()
        )
    }

    fun handlePickSubject(context: CallbackContext, subjectId: Long) {
        val editMessage = EditMessageText.builder()
            .edit(context.message)
            .text("Введите название лабы (ответом на это сообщение):")
            .build()

        enterLabNameState.setChatData(context.chatId, subjectId)
        stateManager.setHandler(context.chatId, enterLabNameState)
        telegram.execute(editMessage)
    }

    fun handleSubjectsPage(context: CallbackContext, page: Int) {
        val editMessage = EditMessageText.builder()
            .edit(context.message)
            .withTextAndInlineKeyboard(
                "Выберите предмет (стр. $page): ",
                getSubjectListButtons(context.group!!.subjects, page)
            )

        telegram.execute(editMessage)
    }

    override fun prefix() = command()

    override fun command() = NAME

    override fun scope() = Scope.GROUP

    companion object {
        const val NAME = "new_lab"
    }
}