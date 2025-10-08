package me.alllexey123.itmoqueue.bot.callback

import me.alllexey123.itmoqueue.model.enums.MergedQueueType
import me.alllexey123.itmoqueue.model.enums.QueueType

sealed class CallbackData(val handlerPrefix: String, val actionPrefix: String) {
    open fun toPayload(): List<String> = emptyList()

    companion object {
        private val deserializerRegistry = mutableMapOf<String, (List<String>) -> CallbackData>()

        private fun register(command: String, action: String, creator: (List<String>) -> CallbackData) {
            val key = "$command:$action"
            if (deserializerRegistry.containsKey(key)) {
                throw IllegalStateException("Duplicate callback data registration for key: $key")
            }
            deserializerRegistry[key] = creator
        }

        fun findCreator(key: String): ((List<String>) -> CallbackData)? {
            return deserializerRegistry[key]
        }

        init {
            // Lab callbacks
            register("lab", "details") { p -> ShowLabDetails(p[0].toLong()) }
            register("lab", "delete_ask") { LabDeleteAsk() }
            register("lab", "delete_confirm") { LabDeleteConfirm() }
            register("lab", "delete_cancel") { LabDeleteCancel() }
            register("lab", "add_to_queue") { LabAddToQueue() }
            register("lab", "add_to_queue_cancel") { LabAddToQueueCancel() }
            register("lab", "remove_from_queue") { LabRemoveFromQueue() }
            register("lab", "mark_queue_entry_done") { LabMarkEntryDone() }
            register("lab", "pin") { LabPin() }
            register("lab", "edit") { LabEditName() }
            register("lab", "select_attempt") { p -> LabSelectAttempt(p[0].toInt()) }
            register("lab", "queue_type_ask") { LabQueueTypeAsk() }
            register("lab", "queue_type_select") { p -> LabQueueTypeSelect(QueueType.valueOf(p[0])) }

            // Lab List callbacks
            register("lab_list", "page") { p -> ShowLabList(p.getOrNull(0)?.toInt() ?: 1) }

            // Subject callbacks
            register("subject", "details") { p -> ShowSubjectDetails(p[0].toLong()) }
            register("subject", "delete_ask") { SubjectDeleteAsk() }
            register("subject", "delete_confirm") { SubjectDeleteConfirm() }
            register("subject", "delete_cancel") { SubjectDeleteCancel() }
            register("subject", "edit") { SubjectEdit() }
            register("subject", "labs") { ShowSubjectLabsList() }
            register("subject", "queue_type_ask") { SubjectQueueTypeAsk() }
            register("subject", "queue_type_select") { p -> SubjectQueueTypeSelect(p.getOrNull(0)?.let { MergedQueueType.valueOf(it) }) }

            // Subject List callbacks
            register("subject_list", "page") { p -> ShowSubjectList(p.getOrNull(0)?.toInt() ?: 1) }

            // New Lab callbacks
            register("new_lab", "select_subject") { p -> NewLabSelectSubject(p[0].toLong()) }
            register("new_lab", "select_subject_page") { p -> NewLabSelectSubjectPage(p[0].toInt()) }
            register("new_lab", "cancel") { NewLabCancel() }
        }
    }
}


// Lab callbacks
data class ShowLabDetails(val labId: Long) : CallbackData("lab", "details") {
    override fun toPayload() = listOf(labId.toString())
}
class LabDeleteAsk : CallbackData("lab", "delete_ask")
class LabDeleteConfirm : CallbackData("lab", "delete_confirm")
class LabDeleteCancel : CallbackData("lab", "delete_cancel")
class LabAddToQueue : CallbackData("lab", "add_to_queue")
class LabAddToQueueCancel : CallbackData("lab", "add_to_queue_cancel")
class LabRemoveFromQueue : CallbackData("lab", "remove_from_queue")
class LabMarkEntryDone : CallbackData("lab", "mark_queue_entry_done")
class LabPin : CallbackData("lab", "pin")
class LabEditName : CallbackData("lab", "edit")
data class LabSelectAttempt(val attempt: Int) : CallbackData("lab", "select_attempt") {
    override fun toPayload() = listOf(attempt.toString())
}
class LabQueueTypeAsk : CallbackData("lab", "queue_type_ask")
data class LabQueueTypeSelect(val type: QueueType) : CallbackData("lab", "queue_type_select") {
    override fun toPayload() = listOf(type.name)
}

// Lab List callbacks
data class ShowLabList(val page: Int = 1) : CallbackData("lab_list", "page") {
    override fun toPayload() = listOf(page.toString())
}

// Subject callbacks
data class ShowSubjectDetails(val subjectId: Long) : CallbackData("subject", "details") {
    override fun toPayload() = listOf(subjectId.toString())
}
class SubjectDeleteAsk : CallbackData("subject", "delete_ask")
class SubjectDeleteConfirm : CallbackData("subject", "delete_confirm")
class SubjectDeleteCancel : CallbackData("subject", "delete_cancel")
class SubjectEdit : CallbackData("subject", "edit")
class ShowSubjectLabsList : CallbackData("subject", "labs")
class SubjectQueueTypeAsk : CallbackData("subject", "queue_type_ask")
data class SubjectQueueTypeSelect(val type: MergedQueueType?) : CallbackData("subject", "queue_type_select") {
    override fun toPayload() = type?.let { listOf(it.name) } ?: emptyList()
}

// Subject List callbacks
data class ShowSubjectList(val page: Int = 1) : CallbackData("subject_list", "page") {
    override fun toPayload() = listOf(page.toString())
}

// New Lab callbacks
data class NewLabSelectSubject(val subjectId: Long) : CallbackData("new_lab", "select_subject") {
    override fun toPayload() = listOf(subjectId.toString())
}
data class NewLabSelectSubjectPage(val page: Int) : CallbackData("new_lab", "select_subject_page") {
    override fun toPayload() = listOf(page.toString())
}
class NewLabCancel : CallbackData("new_lab", "cancel")


