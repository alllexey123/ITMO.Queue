package me.alllexey123.itmoqueue.bot.callback

import org.springframework.stereotype.Component

sealed class CallbackData(val commandPrefix: String, val actionPrefix: String) {

    // list_labs
    data class SelectLab(val labId: Long) : CallbackData("list_labs", "select")
    data class ShowLabsPage(val page: Int) : CallbackData("list_labs", "page")
    class DeleteLab : CallbackData("list_labs", "delete")
    class AddToQueue : CallbackData("list_labs", "add_to_queue")
    class AddToQueueCancel : CallbackData("list_labs", "add_to_queue_cancel")
    class RemoveFromQueue : CallbackData("list_labs", "remove_from_queue")
    class MarkQueueEntryDone : CallbackData("list_labs", "mark_queue_entry_done")
    class ShowLabsList : CallbackData("list_labs", "show_labs_list")
    class PinLab : CallbackData("list_labs", "pin_lab")
    class EditLab : CallbackData("list_labs", "edit_lab")
    data class SelectAttemptNumber(val attempt: Int) : CallbackData("list_labs", "select_attempt_number")

    // list_subjects
    data class SelectSubject(val subjectId: Long) : CallbackData("list_subjects", "select")
    data class ShowSubjectsPage(val page: Int) : CallbackData("list_subjects", "page")
    class DeleteSubject : CallbackData("list_subjects", "delete")
    class EditSubject : CallbackData("list_subjects", "edit")
    class ShowSubjectsList : CallbackData("list_subjects", "show_subjects_list")

    // new_lab
    data class NewLabPickSubject(val subjectId: Long) : CallbackData("new_lab", "pick_subject")
    data class NewLabSubjectsPage(val page: Int) : CallbackData("new_lab", "subjects_page")
    class NewLabCancel : CallbackData("new_lab", "cancel")

}

@Component
class CallbackDataSerializer : ICallbackDataSerializer {

    override fun serialize(data: CallbackData): String {
        val prefix = data.commandPrefix + ":" + data.actionPrefix
        val additionalData = when (data) {
            is CallbackData.SelectLab -> "${data.labId}"
            is CallbackData.ShowLabsPage -> "${data.page}"
            is CallbackData.DeleteLab -> ""
            is CallbackData.AddToQueue -> ""
            is CallbackData.AddToQueueCancel -> ""
            is CallbackData.RemoveFromQueue -> ""
            is CallbackData.MarkQueueEntryDone -> ""
            is CallbackData.ShowLabsList -> ""
            is CallbackData.PinLab -> ""
            is CallbackData.EditLab -> ""
            is CallbackData.SelectAttemptNumber -> "${data.attempt}"

            is CallbackData.SelectSubject -> "${data.subjectId}"
            is CallbackData.ShowSubjectsPage -> "${data.page}"
            is CallbackData.DeleteSubject -> ""
            is CallbackData.EditSubject -> ""
            is CallbackData.ShowSubjectsList -> ""

            is CallbackData.NewLabPickSubject -> "${data.subjectId}"
            is CallbackData.NewLabSubjectsPage -> "${data.page}"
            is CallbackData.NewLabCancel -> ""
        }
        return if (additionalData.isEmpty()) prefix else "${prefix}:${additionalData}"
    }

    override fun deserialize(rawData: String): CallbackData {
        var parts = rawData.split(":")
        val command = parts.getOrNull(0)
        val action = parts.getOrNull(1)

        parts = parts.drop(2)
        return when ("$command:$action") {
            "list_labs:select" -> CallbackData.SelectLab(parts[0].toLong())
            "list_labs:page" -> CallbackData.ShowLabsPage(parts[0].toInt())
            "list_labs:delete" -> CallbackData.DeleteLab()
            "list_labs:add_to_queue" -> CallbackData.AddToQueue()
            "list_labs:add_to_queue_cancel" -> CallbackData.AddToQueueCancel()
            "list_labs:remove_from_queue" -> CallbackData.RemoveFromQueue()
            "list_labs:mark_queue_entry_done" -> CallbackData.MarkQueueEntryDone()
            "list_labs:show_labs_list" -> CallbackData.ShowLabsList()
            "list_labs:pin_lab" -> CallbackData.PinLab()
            "list_labs:edit_lab" -> CallbackData.EditLab()
            "list_labs:select_attempt_number" -> CallbackData.SelectAttemptNumber(parts[0].toInt())

            "list_subjects:select" -> CallbackData.SelectSubject(parts[0].toLong())
            "list_subjects:page" -> CallbackData.ShowSubjectsPage(parts[0].toInt())
            "list_subjects:delete" -> CallbackData.DeleteSubject()
            "list_subjects:edit" -> CallbackData.EditSubject()
            "list_subjects:show_subjects_list" -> CallbackData.ShowSubjectsList()

            "new_lab:pick_subject" -> CallbackData.NewLabPickSubject(parts[0].toLong())
            "new_lab:subjects_page" -> CallbackData.NewLabSubjectsPage(parts[0].toInt())
            "new_lab:cancel" -> CallbackData.NewLabCancel()

            else -> throw IllegalArgumentException("Unknown callback data")
        }
    }
}

interface ICallbackDataSerializer {

    fun serialize(data: CallbackData): String

    fun deserialize(rawData: String): CallbackData
}