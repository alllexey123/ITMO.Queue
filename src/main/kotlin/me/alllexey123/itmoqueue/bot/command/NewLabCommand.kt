package me.alllexey123.itmoqueue.bot.command

import me.alllexey123.itmoqueue.bot.Scope
import me.alllexey123.itmoqueue.bot.callback.CallbackHandler
import me.alllexey123.itmoqueue.bot.extensions.replyTo
import me.alllexey123.itmoqueue.model.Subject
import me.alllexey123.itmoqueue.services.GroupService
import me.alllexey123.itmoqueue.services.TelegramService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow
import me.alllexey123.itmoqueue.bot.extensions.*
import me.alllexey123.itmoqueue.bot.state.EnterLabNameState
import me.alllexey123.itmoqueue.bot.state.StateManager

@Component
class NewLabCommand(
    private val telegramService: TelegramService,
    private val groupService: GroupService,
    private val stateManager: StateManager,
    private val enterLabNameState: EnterLabNameState
) :
    CommandHandler, CallbackHandler {

    override fun handle(message: Message) {
        val chat = message.chat
        val group = groupService.getOrCreateByChatId(chat.id)
        val sendMessageBuilder = SendMessage.builder()
            .replyTo(message)

        val subjects = group.subjects
        if (subjects.isNotEmpty()) {
            sendMessageBuilder.chatId(message.chatId)
                .withInlineKeyboard("Выберите предмет: ", getSubjectListButtons(subjects))
        } else {
            sendMessageBuilder.text("Не добавлен ни один предмет\n\nДобавить предмет - /new_subject")
        }

        telegramService.client.execute(sendMessageBuilder.build())
    }

    fun getSubjectListButtons(subjects: List<Subject>, page: Int = 1): InlineKeyboardMarkup {
        val perPage = 5

        val pageSubjects = subjects.drop((page - 1) * perPage).take(perPage)

        val rows = pageSubjects.map { subject ->
            inlineRowButton(subject.name, addPrefix("subject ${subject.id}"))
        }.toMutableList()

        val pagination = mutableListOf<InlineKeyboardButton>()
        if (page > 1) {
            pagination.add(inlineButton("⬅\uFE0F", addPrefix("subject_page ${page - 1}")))
        }
        if (page * perPage < subjects.size) {
            pagination.add(inlineButton("➡\uFE0F", addPrefix("subject_page ${page + 1}")))
        }
        if (pagination.isNotEmpty()) {
            rows.add(InlineKeyboardRow(pagination))
        }

        rows.add(inlineRowButton("❌", addPrefix("cancel")))

        return InlineKeyboardMarkup(rows)
    }


    override fun handle(callbackQuery: CallbackQuery) {
        val split = removePrefix(callbackQuery.data).split(" ")
        val message = callbackQuery.message
        val group = groupService.getOrCreateByChatId(message.chatId)
        when (split[0]) {
            "subject" -> {
                val editMessage = EditMessageText.builder()
                    .edit(message)
                    .text("Введите название лабы (ответом на это сообщение):")
                    .build()

                enterLabNameState.setChatData(message.chatId, removePrefix(callbackQuery.data))
                stateManager.setHandler(message.chatId, enterLabNameState)

                telegramService.client.execute(editMessage)
            }

            "subject_page" -> {
                val page = split[1].toInt()
                val editMessage = EditMessageText.builder()
                    .edit(message)
                    .withInlineKeyboard("Выберите предмет (стр. $page): ", getSubjectListButtons(group.subjects, page))

                telegramService.client.execute(editMessage)
            }

            "cancel" -> {
                telegramService.client.execute(
                    EditMessageText.builder()
                        .text("Создание лабы отменено")
                        .edit(message)
                        .build()
                )
            }
        }
    }

    override fun prefix() = command()

    override fun command() = "new_lab"

    override fun scope() = Scope.GROUP
}