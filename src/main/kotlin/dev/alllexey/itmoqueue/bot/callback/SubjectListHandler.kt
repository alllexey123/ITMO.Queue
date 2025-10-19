package dev.alllexey.itmoqueue.bot.callback

import dev.alllexey.itmoqueue.bot.Scope
import dev.alllexey.itmoqueue.bot.callback.CallbackUtils.Companion.SUBJECT_LIST_PAGE_KEY
import dev.alllexey.itmoqueue.bot.extensions.edit
import dev.alllexey.itmoqueue.bot.extensions.getInt
import dev.alllexey.itmoqueue.bot.extensions.markdown
import dev.alllexey.itmoqueue.bot.extensions.toThread
import dev.alllexey.itmoqueue.bot.extensions.withTextAndInlineKeyboard
import dev.alllexey.itmoqueue.bot.view.SubjectListView
import dev.alllexey.itmoqueue.model.Group
import dev.alllexey.itmoqueue.model.ManagedMessage
import dev.alllexey.itmoqueue.model.enums.MessageType
import dev.alllexey.itmoqueue.services.ManagedMessageService
import dev.alllexey.itmoqueue.services.Telegram
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage

@Component
class SubjectListHandler(
    private val callbackUtils: CallbackUtils,
    private val telegram: Telegram,
    private val managedMessageService: ManagedMessageService,
    private val subjectListView: SubjectListView
) : CallbackHandler, ICallbackUtils by callbackUtils {
    override fun handleCallback(context: CallbackContext) {
        when (val data = context.data) {
            is CallbackData.ShowSubjectList -> handle(context, data.page)
            else -> {}
        }
    }

    private fun handle(context: CallbackContext, page: Int) {
        val group = context.group ?: getGroupOrDelete(context) ?: return
        updateGroupListMessage(group, context.managedMessage, page)
    }

    fun sendGroupList(group: Group, page: Int = 0, threadId: Int? = null) {
        val subjects = group.subjects
        val text = subjectListView.getSubjectListText(subjects, page)
        val keyboard = subjectListView.getSubjectListKeyboard(subjects, page)
        val send = SendMessage.builder()
            .toThread(group.settings.mainThreadId ?: threadId)
            .markdown()
            .chatId(group.chatId)
            .text(text)
            .replyMarkup(keyboard)

        managedMessageService.register(
            sentMessage = telegram.execute(send.build()),
            type = MessageType.SUBJECT_DETAILS,
            metadata = groupMetadata(group).also {
                it[SUBJECT_LIST_PAGE_KEY] = page
            }
        )
    }

    fun updateGroupListMessage(group: Group, managedMessage: ManagedMessage, page: Int? = null) {
        val realPage = page ?: managedMessage.metadata.getInt(SUBJECT_LIST_PAGE_KEY, 1)
        val subjects = group.subjects
        val text = subjectListView.getSubjectListText(subjects, realPage)
        val keyboard = subjectListView.getSubjectListKeyboard(subjects, realPage)
        val editMessage = managedMessage.edit()
            .markdown()
            .withTextAndInlineKeyboard(text, keyboard)
        telegram.execute(editMessage)
        managedMessage.metadata[SUBJECT_LIST_PAGE_KEY] = realPage
        managedMessage.messageType = MessageType.GROUP_SUBJECT_LIST
    }

    override fun prefix() = "subject_list"

    override fun scope() = Scope.ANY
}