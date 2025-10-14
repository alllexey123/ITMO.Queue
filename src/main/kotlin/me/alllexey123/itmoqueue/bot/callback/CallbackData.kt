package me.alllexey123.itmoqueue.bot.callback

import me.alllexey123.itmoqueue.bot.view.GroupSettingsView
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
            // Group List Callbacks
            register("group_list", "menu") { GroupListMenu() }
            register("group_list", "select") { p -> GroupListSelect(p[0].toLong()) }
            register("group_list", "back") { GroupListBack() }

            // Lab callbacks
            register("lab", "details") { p -> LabDetailsShow(p[0].toLong()) }
            register("lab", "refresh") { LabDetailsRefresh() }
            register("lab", "remove_from_queue") { LabRemoveFromQueue() }
            register("lab", "mark_queue_entry_done") { LabMarkEntryDone() }
            register("lab", "pin") { LabPin() }

            // Lab Edit callbacks
            register("lab_edit", "menu") { LabEditMenu() }
            register("lab_edit", "change_name") { LabEditChangeName() }

            // Lab Delete callbacks
            register("lab_delete", "ask") { LabDeleteAsk() }
            register("lab_delete", "confirm") { LabDeleteConfirm() }
            register("lab_delete", "cancel") { LabDeleteCancel() }

            // Lab Queue Type callbacks
            register("lab_queue_type", "queue_type_ask") { p -> LabQueueTypeAsk(p[0].toBoolean()) }
            register("lab_queue_type", "queue_type_select") { p -> LabQueueTypeSelect(p[0].toBoolean(), QueueType.valueOf(p[1])) }

            // Lab Add To Queue callbacks
            register("lab_add_to_queue", "ask") { LabAddToQueueAsk() }
            register("lab_add_to_queue", "select_attempt") { p -> LabAddToQueueAttempt(p[0].toInt()) }
            register("lab_add_to_queue", "cancel") { LabAddToQueueCancel() }

            // Lab List callbacks
            register("lab_list", "page") { p -> ShowLabList(p.getOrNull(0)?.toInt() ?: 0) }

            // Subject callbacks
            register("subject", "details") { p -> SubjectDetailsShow(p[0].toLong()) }
            register("subject", "refresh") { SubjectDetailsRefresh() }
            register("subject", "change_name") { SubjectChangeName() }
            register("subject", "labs") { SubjectShowLabList() }

            // Subject Edit callbacks
            register("subject_edit", "menu") { SubjectEditMenu() }
            register("subject_edit", "change_name") { SubjectEditChangeName() }

            // Subject Delete callbacks
            register("subject_delete", "ask") { SubjectDeleteAsk() }
            register("subject_delete", "confirm") { SubjectDeleteConfirm() }
            register("subject_delete", "cancel") { SubjectDeleteCancel() }

            // Subject Queue Type callbacks
            register("subject_queue_type", "ask") { p -> SubjectQueueTypeAsk(p[0].toBoolean()) }
            register("subject_queue_type", "select") { p ->
                SubjectQueueTypeSelect(p[0].toBoolean(), MergedQueueType.valueOf(p[1]))
            }

            // Subject List callbacks
            register("subject_list", "page") { p -> ShowSubjectList(p.getOrNull(0)?.toInt() ?: 0) }

            // New callbacks
            register("new", "menu") { NewMenu() }

            // New subject callbacks
            register("new_subject", "menu") { NewSubjectMenu() }

            // New Lab callbacks
            register("new_lab", "select_subject") { p -> NewLabSelectSubject(p[0].toLong()) }
            register("new_lab", "select_subject_page") { p -> NewLabSelectSubjectPage(p[0].toInt()) }
            register("new_lab", "send_to_group") {p -> NewLabSendToGroup(p[0].toLong()) }
            register("new_lab", "cancel") { NewLabCancel() }

            // Settings callbacks
            register("settings", "menu") { SettingsMenu() }
            register("settings", "select_thread") { SettingsSelectThread() }
            register("settings", "reset_thread") { SettingsResetThread() }
            register("settings", "switch_setting") { p ->
                SettingsSwitchSetting(
                    GroupSettingsView.SwitchSetting.valueOf(
                        p[0]
                    ), p[1].toBoolean()
                )
            }
        }
    }

    // Group List callbacks
    class GroupListMenu() : CallbackData("group_list", "menu")
    data class GroupListSelect(val groupId: Long) : CallbackData("group_list", "select") {
        override fun toPayload() = listOf(groupId.toString())
    }
    class GroupListBack : CallbackData("group_list", "back")

    // Lab callbacks
    data class LabDetailsShow(val labId: Long) : CallbackData("lab", "details") {
        override fun toPayload() = listOf(labId.toString())
    }

    class LabDetailsRefresh() : CallbackData("lab", "refresh")
    class LabRemoveFromQueue : CallbackData("lab", "remove_from_queue")
    class LabMarkEntryDone : CallbackData("lab", "mark_queue_entry_done")
    class LabPin : CallbackData("lab", "pin")

    // Lab Edit callbacks
    class LabEditMenu : CallbackData("lab_edit", "menu")
    class LabEditChangeName : CallbackData("lab_edit", "change_name")

    // Lab Delete callbacks
    class LabDeleteAsk : CallbackData("lab_delete", "ask")
    class LabDeleteConfirm : CallbackData("lab_delete", "confirm")
    class LabDeleteCancel : CallbackData("lab_delete", "cancel")

    // Lab Queue Type callbacks
    data class LabQueueTypeAsk(val selectingDefault: Boolean) : CallbackData("lab_queue_type", "queue_type_ask") {
        override fun toPayload(): List<String> = listOf(selectingDefault.toString())
    }
    data class LabQueueTypeSelect(val selectingDefault: Boolean, val type: QueueType) : CallbackData("lab_queue_type", "queue_type_select") {
        override fun toPayload() = listOf(selectingDefault.toString(), type.name)
    }

    // Lab Add To Queue callbacks
    class LabAddToQueueAsk : CallbackData("lab_add_to_queue", "ask")
    data class LabAddToQueueAttempt(val attempt: Int) : CallbackData("lab_add_to_queue", "select_attempt") {
        override fun toPayload() = listOf(attempt.toString())
    }

    class LabAddToQueueCancel : CallbackData("lab_add_to_queue", "cancel")

    // Lab List callbacks
    data class ShowLabList(val page: Int = 0) : CallbackData("lab_list", "page") {
        override fun toPayload() = listOf(page.toString())
    }

    // Subject callbacks
    data class SubjectDetailsShow(val subjectId: Long) : CallbackData("subject", "details") {
        override fun toPayload() = listOf(subjectId.toString())
    }

    class SubjectDetailsRefresh() : CallbackData("subject", "refresh")
    class SubjectChangeName : CallbackData("subject", "change_name")
    class SubjectShowLabList : CallbackData("subject", "labs")

    // Subject Edit callbacks
    class SubjectEditMenu : CallbackData("subject_edit", "menu")
    class SubjectEditChangeName : CallbackData("subject_edit", "change_name")

    // Subject Delete callbacks
    class SubjectDeleteAsk : CallbackData("subject_delete", "ask")
    class SubjectDeleteConfirm : CallbackData("subject_delete", "confirm")
    class SubjectDeleteCancel : CallbackData("subject_delete", "cancel")

    // Subject Queue Type callbacks
    data class SubjectQueueTypeAsk(val selectingDefault: Boolean) : CallbackData("subject_queue_type", "ask") {
        override fun toPayload(): List<String> = listOf(selectingDefault.toString())
    }
    data class SubjectQueueTypeSelect(val selectingDefault: Boolean, val type: MergedQueueType) : CallbackData("subject_queue_type", "select") {
        override fun toPayload() = listOf(selectingDefault.toString(), type.name)
    }

    // Subject List callbacks
    data class ShowSubjectList(val page: Int = 0) : CallbackData("subject_list", "page") {
        override fun toPayload() = listOf(page.toString())
    }

    // New callbacks
    class NewMenu : CallbackData("new", "menu")

    // New Subject callbacks
    class NewSubjectMenu : CallbackData("new_subject", "menu")

    // New Lab callbacks
    data class NewLabSelectSubject(val subjectId: Long) : CallbackData("new_lab", "select_subject") {
        override fun toPayload() = listOf(subjectId.toString())
    }

    data class NewLabSelectSubjectPage(val page: Int = 0) : CallbackData("new_lab", "select_subject_page") {
        override fun toPayload() = listOf(page.toString())
    }

    data class NewLabSendToGroup(val labId: Long) : CallbackData("new_lab", "send_to_group") {
        override fun toPayload() = listOf(labId.toString())
    }

    class NewLabCancel : CallbackData("new_lab", "cancel")

    // Settings callbacks
    class SettingsMenu() : CallbackData("settings", "menu")
    class SettingsSelectThread() : CallbackData("settings", "select_thread")
    class SettingsResetThread() : CallbackData("settings", "reset_thread")
    data class SettingsSwitchSetting(val setting: GroupSettingsView.SwitchSetting, val newVal: Boolean) :
        CallbackData("settings", "switch_setting") {
        override fun toPayload() = listOf(setting.name, newVal.toString())
    }
}

